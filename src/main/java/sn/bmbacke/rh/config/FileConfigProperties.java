package sn.bmbacke.rh.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "spring.application.file")
@Data
public class FileConfigProperties {

    private Uploads uploads = new Uploads();

    @Data
    public static class Uploads {
        private String photosOutputPath;
        private Map<String, List<String>> allowedMimeTypes = new HashMap<>();
    }
}