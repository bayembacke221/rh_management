package sn.bmbacke.rh.payload.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartementUpdateDTO {
    private String name;
    private String description;
    private String code;
    private Long managerId;
    private Long parentDepartementId;
    private Boolean active;
}
