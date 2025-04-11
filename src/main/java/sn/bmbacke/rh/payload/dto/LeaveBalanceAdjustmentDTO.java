package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour ajuster un solde de congés
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceAdjustmentDTO {
    private Float adjustedAmount;
    private String adjustmentReason;
}
