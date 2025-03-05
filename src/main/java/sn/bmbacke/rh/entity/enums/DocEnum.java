package sn.bmbacke.rh.entity.enums;

import java.util.Set;

public enum DocEnum {
    CONTRACT("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    CV("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    ID_CARD("image/jpeg", "image/png"),
    CERTIFICATE("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    OTHER("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "image/jpeg", "image/png");

    private final Set<String> allowedMimeTypes;

    DocEnum(String... mimeTypes) {
        this.allowedMimeTypes = Set.of(mimeTypes);
    }

    public boolean isValidMimeType(String mimeType) {
        return allowedMimeTypes.contains(mimeType);
    }
}
