package sn.bmbacke.rh.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;

@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "departements")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"manager", "parentDepartement"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Departement extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column(nullable = true)
    private String description;
    @Column(nullable = false)
    private String code;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @JsonIgnoreProperties({"departement", "position", "manager", "user"})
    private Employee manager;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_departement_id")
    @JsonIgnoreProperties({"manager", "parentDepartement"})
    private Departement parentDepartement;
    private Boolean active;
}
