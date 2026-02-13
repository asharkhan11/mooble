package in.ashar.mooble.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import in.ashar.mooble.utility.enums.Standard;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tuition_class")
public class TuitionClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int tuitionClassId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Standard standard;

    private char section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tuitionId")
    @ToString.Exclude
    @JsonBackReference("tuition-tc")
    private Tuition2 tuition;

    // ✅ Correct bidirectional mapping with students
    @ManyToMany(mappedBy = "tuitionClasses", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    private List<Student2> students = new ArrayList<>();

    // ✅ Teachers relationship remains unchanged
    @ManyToMany(mappedBy = "tuitionClasses", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    private List<Teacher2> teachers = new ArrayList<>();

    @OneToMany(mappedBy = "tuitionClass", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    private List<Subject2> subjects = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TuitionClass that)) return false;
        return tuitionClassId != 0 && tuitionClassId == that.tuitionClassId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tuitionClassId);
    }
}
