package sn.bmbacke.rh.payload.dto;

import lombok.*;
import sn.bmbacke.rh.entity.enums.Gender;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateDTO {
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate birthDate;
    private Gender gender;
    private String address;
    private String city;
    private String socialSecurityNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String bankAccountInfo;
    private Long departementId;
    private Long positionId;
    private Long managerId;
}
