package sn.bmbacke.rh.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;
import sn.bmbacke.rh.entity.enums.LeaveStatus;
import sn.bmbacke.rh.entity.enums.LeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "leaves")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Leave extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "leave_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @ManyToOne
    @JoinColumn(name = "approved_by_id")
    private Employee approvedBy;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "half_day")
    private Boolean halfDay;

    @Column(name = "attachments")
    private String attachments;

    /**
     * Calcule et met à jour la durée du congé en jours
     */
    @PrePersist
    @PreUpdate
    public void calculateDurationDays() {
        if (startDate != null && endDate != null) {
            // Calculer le nombre de jours entre startDate et endDate (inclusif)
            long days = endDate.toEpochDay() - startDate.toEpochDay() + 1;

            // Si c'est une demi-journée, ajuster à 0.5 jour
            if (Boolean.TRUE.equals(halfDay) && days == 1) {
                this.durationDays = 0;
            } else {
                this.durationDays = (int) days;
            }
        }
    }
}
