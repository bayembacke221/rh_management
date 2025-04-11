package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * DTO pour les données d'un rapport
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDataDTO {
    private Object data;
    private String format;
    private String filename;
}
