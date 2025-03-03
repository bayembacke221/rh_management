package sn.bmbacke.rh.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    @ManyToOne
    @JoinColumn(name = "departement_id")
    private Departement department;
    @Column(nullable = true)
    private String responsibilities;
    @Column(nullable = true)
    private String requirements;
    @Column(nullable = true)
    private Boolean active;
}
