package sn.bmbacke.rh.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;
import sn.bmbacke.rh.entity.enums.LeaveType;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "leave_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "leave_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "initial_balance", nullable = false)
    private Float initialBalance;

    @Column(name = "used_balance", nullable = false)
    private Float usedBalance;

    @Column(name = "adjusted_balance")
    private Float adjustedBalance;

    @Column(name = "adjustment_reason")
    private String adjustmentReason;

    /**
     * Calcule le solde actuel disponible pour ce type de congé
     */
    @Transient
    public Float getCurrentBalance() {
        float balance = initialBalance - usedBalance;
        if (adjustedBalance != null) {
            balance += adjustedBalance;
        }
        return Math.max(0, balance); // Pas de solde négatif
    }
}

