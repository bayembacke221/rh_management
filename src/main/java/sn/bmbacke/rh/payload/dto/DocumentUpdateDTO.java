package sn.bmbacke.rh.payload.dto;

import lombok.*;
import sn.bmbacke.rh.entity.enums.DocEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUpdateDTO {
    private String name;
    private DocEnum type;
}
