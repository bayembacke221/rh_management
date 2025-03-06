package sn.bmbacke.rh.config;


import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("RH API")
                        .version("1.0")
                        .description("API for RH application")
                        .license(
                                new io.swagger.v3.oas.models.info.License()
                                        .name("Apache 2.0")
                                        .url("https://springdoc.org"))
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .email("rh-management@gmail.com")
                                .url("https://rh-management.com"))
                        .termsOfService("https://swagger.io/terms/"))
                .externalDocs(new io.swagger.v3.oas.models.ExternalDocumentation()
                        .description("Find out more about RH")
                        .url("https://rh-management.com"));

    }
}
