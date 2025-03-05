package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.ContratStatus;
import sn.bmbacke.rh.entity.enums.Type;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractCreateDTO {
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Type type;
    private BigDecimal salary;
    private Integer workHoursPerWeek;
    private ContratStatus status;
}
