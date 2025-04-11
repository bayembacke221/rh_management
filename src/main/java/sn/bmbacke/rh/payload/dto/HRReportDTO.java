package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO pour les rapports RH
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HRReportDTO {
    private String reportName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer year;
    private String generatedBy;
    private LocalDate generatedDate;
    private String format;
    private Map<String, Object> parameters;
    private Map<String, Object> data;
}

