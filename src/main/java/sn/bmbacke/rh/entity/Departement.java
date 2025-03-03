package sn.bmbacke.rh.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "departements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Departement extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column(nullable = true)
    private String description;
    @Column(nullable = false)
    private String code;
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;
    @ManyToOne
    @JoinColumn(name = "parent_departement_id")
    private Departement parentDepartement;
    private Boolean active;
}
