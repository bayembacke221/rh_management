package sn.bmbacke.rh.payload.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartementTreeDTO {
    private Long id;
    private String name;
    private String code;
    private EmployeeShortDTO manager;
    private Boolean active;
    private java.util.List<DepartementTreeDTO> children;
}
