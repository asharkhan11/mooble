package in.ashar.mooble.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class ClassJoined {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int tuitionId;
    private LocalDateTime joined;

    public ClassJoined(int tuitionId, LocalDateTime joined) {
        this.tuitionId = tuitionId;
        this.joined = joined;
    }
}
