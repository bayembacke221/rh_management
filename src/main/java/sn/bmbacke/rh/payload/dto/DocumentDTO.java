package sn.bmbacke.rh.payload.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.DocEnum;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private String name;
    private DocEnum type;
    private String path;
    private LocalDateTime uploadDate;
    private Long size;
    private String contentType;
    private String downloadUrl;
}
