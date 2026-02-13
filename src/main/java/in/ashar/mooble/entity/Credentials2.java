package in.ashar.mooble.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Credentials2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private in.ashar.mooble.utility.enums.Role role;

}
