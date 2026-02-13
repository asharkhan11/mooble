package in.ashar.mooble.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Admin2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int adminId;

    private String adminName;
    private String adminEmail;
    private String adminPhoneNumber;
    private String adminAddress;

    @ManyToOne
    private Credentials2 adminCredential;

    @OneToMany(mappedBy = "tuitionAdmin", orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<Tuition2> adminTuition = new ArrayList<>();


    // Subscription
    @OneToOne(mappedBy = "admin")
    @JsonIgnore
    private AdminSubscription subscription;

    @OneToOne(mappedBy = "admin")
    @JsonIgnore
    private AdminUsage usage;

}
