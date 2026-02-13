package in.ashar.mooble.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Tuition2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int tuitionId;

    @Column(nullable = false, unique = true)
    private int tuitionCode;

    private String tuitionName;
    private String tuitionEmail;
    private String tuitionPhoneNumber;
    private String tuitionAddress;
    private String branch;

    @ManyToOne
    @JoinColumn(name = "adminId")
    @ToString.Exclude
    private Admin2 tuitionAdmin;

    @OneToMany(mappedBy = "tuition", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("tuition-tc")
    private List<TuitionClass> tuitionClasses=new ArrayList<>();

    @ElementCollection
    private List<Integer> studentIds=new ArrayList<>();

    @ElementCollection
    private List<Integer> teacherIds=new ArrayList<>();

    @OneToMany(mappedBy = "tuition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course2> courses = new ArrayList<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuition2 that)) return false;
        return tuitionId != 0 && tuitionId == that.tuitionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tuitionId);
    }

}

