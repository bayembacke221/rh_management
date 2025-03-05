package sn.bmbacke.rh.payload.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentEmployeeStatsDTO {
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private Long totalEmployees;
    private Long activeEmployees;
    private Long inactiveEmployees;
    private Long onLeaveEmployees;
}