package sn.bmbacke.rh.payload.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionShortDTO {
    private Long id;
    private String title;
    private String grade;
}
