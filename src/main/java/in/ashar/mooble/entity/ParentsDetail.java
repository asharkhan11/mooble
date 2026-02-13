package in.ashar.mooble.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class ParentsDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long parentId;

    private String name;
    private String relation;
    private String phone;
    private String occupation;
    private String address;

    public ParentsDetail(String name, String relation, String phone, String occupation, String address) {
        this.name = name;
        this.relation = relation;
        this.phone = phone;
        this.occupation = occupation;
        this.address = address;
    }
}

