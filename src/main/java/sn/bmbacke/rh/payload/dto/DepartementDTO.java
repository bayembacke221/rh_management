package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartementDTO {
    private Long id;
    private String name;
    private String description;
    private String code;
    private EmployeeShortDTO manager;
    private DepartementShortDTO parentDepartement;
    private Boolean active;
}

