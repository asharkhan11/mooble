package in.ashar.mooble.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
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
@Table(name = "teacher")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "teacherId"
)
public class Teacher2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int teacherId;

    private String teacherName;
    private String teacherPhoneNumber;
    private String teacherAddress;
    private LocalDate birthDate;

    private String experience;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "credentialId")
    private Credentials2 teacherCredential;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "teacher_subject",
            joinColumns = @JoinColumn(name = "teacherId"),
            inverseJoinColumns = @JoinColumn(name = "subjectId")
    )
    @ToString.Exclude
    private List<Subject2> subjects = new ArrayList<>();

    @ElementCollection
    private List<String> knownSubjects = new ArrayList<>();

    private final LocalDateTime joined = LocalDateTime.now();


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "teacher_course",
            joinColumns = @JoinColumn(name = "teacherId"),
            inverseJoinColumns = @JoinColumn(name = "courseId")
    )
    @ToString.Exclude
    private List<Course2> courses = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "teacher_tuitionclass",
            joinColumns = @JoinColumn(name = "teacherId"),
            inverseJoinColumns = @JoinColumn(name = "tuitionClassId")
    )
    @ToString.Exclude
    private List<TuitionClass> tuitionClasses = new ArrayList<>();


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassJoined> classJoined = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Teacher2 teacher2)) return false;
        return teacherId != 0 && teacherId == teacher2.teacherId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherId);
    }
}
