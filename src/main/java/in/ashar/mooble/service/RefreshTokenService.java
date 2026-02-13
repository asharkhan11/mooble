package in.ashar.mooble.service;

import in.ashar.mooble.entity.RefreshToken;
import in.ashar.mooble.repository.CredentialsRepository;
import in.ashar.mooble.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(String userEmail) {
        // Remove old tokens for user
        refreshTokenRepository.deleteByUserEmail(userEmail);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserEmail(userEmail);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshToken.setToken(generateRandomToken());

        return refreshTokenRepository.save(refreshToken);
    }

    public boolean isValid(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> rt.getExpiryDate().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    public Optional<String> getUserEmailFromToken(String token) {
        return refreshTokenRepository.findByToken(token).map(RefreshToken::getUserEmail);
    }

    public void deleteByUserEmail(String userEmail) {
        refreshTokenRepository.deleteByUserEmail(userEmail);
    }

    private String generateRandomToken() {
        return UUID.randomUUID().toString();
    }
}
