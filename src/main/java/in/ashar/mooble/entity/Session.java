package in.ashar.mooble.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import in.ashar.mooble.utility.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "sessions",
        indexes = {

                // 🔹 Teacher conflict (teacher + date + time + status)
                @Index(
                        name = "idx_session_teacher_time",
                        columnList = "teacher_id, date, start_time, end_time, status"
                ),

                // 🔹 Tuition class / subject conflict
                @Index(
                        name = "idx_session_class_time",
                        columnList = "tuition_class_id, date, start_time, end_time, status"
                ),

                // 🔹 Course conflict
                @Index(
                        name = "idx_session_course_time",
                        columnList = "course_id, date, start_time, end_time, status"
                ),

                // 🔹 Subject overlap (course ↔ subject)
                @Index(
                        name = "idx_session_subject_time",
                        columnList = "subject_id, date, start_time, end_time, status"
                ),

                // 🔹 Recurrence operations (bulk edit / cancel)
                @Index(
                        name = "idx_session_recurrence_group",
                        columnList = "recurrence_group_id"
                ),
                // 🔹 Course conflict
                @Index(
                        name = "idx_session_admin_date_time_status",
                        columnList = "created_by, date, start_time, status"
                )

        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tuition_class_id", nullable = false)
    private TuitionClass tuitionClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject2 subject;   // optional

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course2 course;     // optional

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher2 teacher;

    @Column(name = "recurrence_group_id")
    private String recurrenceGroupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.PLANNED;

    private Integer createdBy;
    private Integer updatedBy;

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Attendance attendance;


    @PrePersist
    @PreUpdate
    private void validate() {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalStateException("Session startTime must be before endTime");
        }

        boolean hasSubject = subject != null;
        boolean hasCourse = course != null;

        if (hasSubject == hasCourse) {
            throw new IllegalStateException(
                    "A session must be linked to either subject or course, but not both."
            );
        }
    }
}
