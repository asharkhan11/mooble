package in.ashar.mooble.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "admin_id", nullable = false, unique = true)
    @JsonIgnore
    private Admin2 admin;

    @Column(nullable = false)
    private int usedMembers; // students + teachers + admin

    @Column(nullable = false)
    private long usedStorageKb;
}
