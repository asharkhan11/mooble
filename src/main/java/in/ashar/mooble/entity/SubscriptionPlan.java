package in.ashar.mooble.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String name; // FREE, ELITE, ENTERPRISE

    @Column(nullable = false)
    private int maxMembers;

    @Column(nullable = false)
    private long maxStorageMb;

    @Column(nullable = false)
    private int pricePerMonth; // in INR (0 for free)
}
