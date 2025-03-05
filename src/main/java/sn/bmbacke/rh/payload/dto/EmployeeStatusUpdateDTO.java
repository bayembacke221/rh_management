package sn.bmbacke.rh.payload.dto;

import lombok.*;
import sn.bmbacke.rh.entity.enums.Status;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStatusUpdateDTO {
    private Status status;
    private LocalDate endDate;
    private String reason;
}
