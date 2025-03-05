package sn.bmbacke.rh.payload.dto;

import lombok.*;
import sn.bmbacke.rh.entity.enums.Gender;
import sn.bmbacke.rh.entity.enums.Status;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateDTO {
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate birthDate;
    private LocalDate hireDate;
    private Gender gender;
    private String address;
    private String city;
    private String socialSecurityNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String bankAccountInfo;
    private Status status;
    private Long departementId;
    private Long positionId;
    private Long managerId;
}
