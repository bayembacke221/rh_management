package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.Gender;
import sn.bmbacke.rh.entity.enums.Status;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate birthDate;
    private LocalDate hireDate;
    private LocalDate endDate;
    private Gender gender;
    private String address;
    private String city;
    private String socialSecurityNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String bankAccountInfo;
    private String cv;
    private Status status;
    private DepartementDTO departement;
    private PositionDTO position;
    private EmployeeShortDTO manager;
}
