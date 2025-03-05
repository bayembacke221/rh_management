package sn.bmbacke.rh.helper.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import sn.bmbacke.rh.entity.enums.DocEnum;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ResourceUtils {

    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        // Documents
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
        MIME_TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        MIME_TYPES.put("txt", "text/plain");

        // Images
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("svg", "image/svg+xml");

        // Autres
        MIME_TYPES.put("zip", "application/zip");
    }

    /**
     * Convertit un tableau d'octets en ressource avec le type MIME approprié
     */
    public Resource createResourceFromBytes(byte[] data, String fileName) {
        if (data == null || data.length == 0) {
            return null;
        }
        return new ByteArrayResource(data);
    }

    /**
     * Détermine le type MIME à partir du nom de fichier
     */
    public String getMimeTypeFromFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        return MIME_TYPES.getOrDefault(extension, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    /**
     * Valide qu'un fichier est du type attendu
     */
    public boolean isValidDocumentType(MultipartFile file, DocEnum docType) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return switch (docType) {
            case CV, DIPLOMA, ADMINISTRATIVE, CERTIFICATE, PAYSLIP, CONTRACT -> contentType.equals("application/pdf") ||
                    contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case IDENTITY -> contentType.equals("application/pdf") ||
                    contentType.equals("image/jpeg") ||
                    contentType.equals("image/png");
            default -> true;
        };
    }
}