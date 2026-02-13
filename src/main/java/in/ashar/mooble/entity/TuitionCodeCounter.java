package in.ashar.mooble.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tuition_code_counter")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TuitionCodeCounter {

    @Id
    private int id;

    @Column(nullable = false)
    private int nextCode;
}
