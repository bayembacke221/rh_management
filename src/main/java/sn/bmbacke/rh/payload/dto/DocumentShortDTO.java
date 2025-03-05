package sn.bmbacke.rh.payload.dto;

import lombok.*;
import sn.bmbacke.rh.entity.enums.DocEnum;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentShortDTO {
    private Long id;
    private String name;
    private DocEnum type;
    private LocalDateTime uploadDate;
}

