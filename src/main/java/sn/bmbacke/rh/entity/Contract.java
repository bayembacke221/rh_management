package sn.bmbacke.rh.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;
import sn.bmbacke.rh.entity.enums.ContratStatus;
import sn.bmbacke.rh.entity.enums.Type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "contract")
@Data
@NoArgsConstructor
public class Contract extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private Type type;
    @Column(name = "salary", nullable = false)
    private BigDecimal salary;
    @Column(name = "work_hours_per_week", nullable = false)
    private Integer workHoursPerWeek;
    @OneToMany(mappedBy = "contract")
    private List<Document> documents;
    private ContratStatus status;
    private String terminationReason;
}
