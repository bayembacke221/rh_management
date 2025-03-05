package sn.bmbacke.rh.payload.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartementShortDTO {
    private Long id;
    private String name;
    private String code;
}

