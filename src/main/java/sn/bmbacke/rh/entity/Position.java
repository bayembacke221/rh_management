package sn.bmbacke.rh.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "positions")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"department"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Position extends BaseEntity {

    @Column(nullable = false)
    private String title;
    @Column(nullable = true)
    private String description;
    @Column(nullable = false)
    private String grade;
    @Column(nullable = false)
    private BigDecimal minSalary;
    @Column(nullable = false)
    private BigDecimal maxSalary;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    @JsonIgnoreProperties({"manager", "parentDepartement"})
    private Departement department;
    @Column(nullable = true)
    private String responsibilities;
    @Column(nullable = true)
    private String requirements;
    @Column(nullable = true)
    private Boolean active;
}
