package sn.bmbacke.rh.payload.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeShortDTO {
    private Long id;
    private String firstName;
    private String lastName;
}