package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.ContratStatus;
import sn.bmbacke.rh.entity.enums.Type;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractShortDTO {
    private Long id;
    private Type type;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContratStatus status;
}
