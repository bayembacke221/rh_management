package sn.bmbacke.rh.event;

import lombok.Builder;
import lombok.Data;
import sn.bmbacke.rh.entity.Document;

import java.time.LocalDateTime;

/**
 * Événement déclenché lors du téléchargement d'un document
 */
@Data
@Builder
public class DocumentUploadedEvent implements SystemEvent {
    private final Long documentId;
    private final Long employeeId;
    private final String documentName;
    private final String documentType;
    private final LocalDateTime eventTime;
    private final Long triggeredBy;

    public static DocumentUploadedEvent fromDocument(Document document, Long triggeredBy) {
        return DocumentUploadedEvent.builder()
                .documentId(document.getId())
                .employeeId(document.getEmployee() != null ? document.getEmployee().getId() : null)
                .documentName(document.getName())
                .documentType(document.getType().name())
                .eventTime(LocalDateTime.now())
                .triggeredBy(triggeredBy)
                .build();
    }

    @Override
    public String getEventType() {
        return "DOCUMENT_UPLOADED";
    }
}