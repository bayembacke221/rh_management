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
@Table(name = "leave_policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicy extends BaseEntity {

    @Column(name = "leave_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "default_days", nullable = false)
    private Float defaultDays;

    @Column(name = "max_consecutive_days")
    private Integer maxConsecutiveDays;

    @Column(name = "min_request_notice_days")
    private Integer minRequestNoticeDays;

    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval;

    @Column(name = "requires_documentation")
    private Boolean requiresDocumentation;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid;

    @Column(name = "carry_forward_allowed")
    private Boolean carryForwardAllowed;

    @Column(name = "max_carry_forward_days")
    private Float maxCarryForwardDays;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
