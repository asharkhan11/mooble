package in.ashar.mooble.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(
    name = "attendance_entry",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"attendance_id", "student_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student2 student;

    @Enumerated(EnumType.STRING)
    private AttendanceMark mark;

    public enum AttendanceMark {
        PRESENT,
        ABSENT,
        LATE,
        EXCUSED
    }

}

