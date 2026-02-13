package in.ashar.mooble.service;

import in.ashar.mooble.entity.PasswordSetupToken;
import in.ashar.mooble.repository.PasswordSetupTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordSetupTokenService {

    private final PasswordSetupTokenRepository repository;

    public PasswordSetupTokenService(PasswordSetupTokenRepository repository) {
        this.repository = repository;
    }

    public String createToken(String email) {
        String token = UUID.randomUUID().toString();
        PasswordSetupToken setupToken = PasswordSetupToken.builder()
                .email(email)
                .token(token)
                .expiry(LocalDateTime.now().plusHours(24))
                .build();
        repository.save(setupToken);
        return token;
    }

    public Optional<PasswordSetupToken> getByToken(String token) {
        return repository.findByToken(token);
    }


    public void deleteToken(String token) {
        repository.deleteByToken(token);
    }
}
