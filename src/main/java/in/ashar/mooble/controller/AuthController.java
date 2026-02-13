package in.ashar.mooble.controller;

import in.ashar.mooble.dto.LoginRequest;
import in.ashar.mooble.entity.Credentials2;
import in.ashar.mooble.repository.CredentialsRepository;
import in.ashar.mooble.security.CustomUserDetailsService;
import in.ashar.mooble.security.JwtService;
import in.ashar.mooble.service.PasswordSetupTokenService;
import in.ashar.mooble.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordSetupTokenService tokenService;
    private final CredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;


    @ResponseBody
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);
        var refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken()
        );
    }


    @ResponseBody
    @PostMapping("/refresh-token")
    public Map<String, String> refreshToken(@RequestParam String refreshToken) {
        if (!refreshTokenService.isValid(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String userEmail = refreshTokenService.getUserEmailFromToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("User not found for this refresh token"));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);
        String newAccessToken = jwtService.generateToken(userDetails);

        return Map.of("accessToken", newAccessToken);
    }

    @GetMapping("/set-password")
    public String showSetPasswordForm(@RequestParam String token, Model model) {
        var setupTokenOpt = tokenService.getByToken(token);
        if (setupTokenOpt.isEmpty() || setupTokenOpt.get().getExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "The password setup link is invalid or has expired.");
            return "pass-error";
        }
        model.addAttribute("token", token);
        return "generate-password";
    }

    @PostMapping("/set-password")
    public String setPassword(@RequestParam String token,
                              @RequestParam("password") String newPassword,
                              @RequestParam("confirmPassword") String confirmPassword,
                              Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);
            return "generate-password";
        }

        var setupTokenOpt = tokenService.getByToken(token);
        if (setupTokenOpt.isEmpty() || setupTokenOpt.get().getExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "The password setup link is invalid or has expired.");
            return "pass-error";
        }

        var setupToken = setupTokenOpt.get();
        var userOpt = credentialsRepository.findByEmail(setupToken.getEmail());
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "User not found for this token.");
            return "pass-error";
        }

        Credentials2 user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        credentialsRepository.save(user);

        tokenService.deleteToken(token);

        return "redirect:/auth/password-success";
    }

    @GetMapping("/password-success")
    public String passwordSuccess(Model model) {
        model.addAttribute("message", "Password set successfully!");
        return "pass-success";
    }
}