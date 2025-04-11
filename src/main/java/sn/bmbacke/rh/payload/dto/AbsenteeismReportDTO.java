package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List; /**
 * DTO pour le rapport d'absentéisme
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenteeismReportDTO {
    private Integer year;
    private Integer totalEmployees;
    private Integer totalWorkingDays;
    private Integer totalAbsenceDays;
    private Double absenteeismRate; // en pourcentage
    private List<MonthlyAbsenteeismDTO> monthlyAbsenteeism;
    private List<DepartmentAbsenteeismDTO> departmentAbsenteeism;
    private List<LeaveTypeAbsenteeismDTO> leaveTypeAbsenteeism;
}
