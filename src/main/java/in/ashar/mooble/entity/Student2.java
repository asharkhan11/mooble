package in.ashar.mooble.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "student")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "studentId"
)
public class Student2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int studentId;

    private String studentName;
    private String studentPhoneNumber;
    private LocalDate birthDate;
    private String studentAddress;

    @Builder.Default
    private final LocalDateTime joined = LocalDateTime.now();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "parentsId")
    private ParentsDetail parentsDetail;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassJoined> classJoined = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "credentialId")
    private Credentials2 studentCredential;

    // ✅ Added cascade to ensure join table gets updated automatically
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "student_tuitionclass",
            joinColumns = @JoinColumn(name = "studentId"),
            inverseJoinColumns = @JoinColumn(name = "tuitionClassId")
    )
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<TuitionClass> tuitionClasses = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_subject",
            joinColumns = @JoinColumn(name = "studentId"),
            inverseJoinColumns = @JoinColumn(name = "subjectId")
    )
    @ToString.Exclude

    @Builder.Default
    private List<Subject2> subjects = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "studentId"),
            inverseJoinColumns = @JoinColumn(name = "courseId")
    )
    @ToString.Exclude
    @Builder.Default
    private List<Course2> courses = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student2 student2)) return false;
        return studentId != 0 && studentId == student2.studentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
}
