//package in.ashar.mooble.security;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {
//
//    private final JwtUtil jwtUtil;
//
//    public CustomAuthSuccessHandler(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        Authentication authentication)
//            throws IOException {
//
//        String email = authentication.getName();
//        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
//
//        String token = jwtUtil.generateToken(email, role);
//
//        response.setContentType("application/json");
//        response.getWriter().write("{\"token\":\"" + token + "\"}");
//    }
//}
