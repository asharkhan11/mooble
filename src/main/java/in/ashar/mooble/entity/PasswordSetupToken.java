package in.ashar.mooble.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_setup_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordSetupToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email; // Link to User's email

    @Column(nullable = false, unique = true)
    private String token;

    private LocalDateTime expiry; // Expire after certain time
}
