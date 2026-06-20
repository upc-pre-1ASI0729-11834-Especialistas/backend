package com.test.backend.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    //Properties
    @Value("${spring.application.name:backend}")
    String applicationName;

    @Value("${documentation.application.description:SafeLab REST API Documentation}")
    String applicationDescription;

    @Value("${documentation.application.version:v1.0.0}")
    String applicationVersion;

    @Bean
    public OpenAPI safeLabOpenApi() {

        // General configuration
        var openApi = new OpenAPI();

        openApi
                .info(new Info()
                        .title(this.applicationName)
                        .description(this.applicationDescription)
                        .version(this.applicationVersion)
                        .license(new License().name("Apache 2.0")
                                .url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("SafeLab Platform Documentation Wiki")
                        .url("https://safelab-platform.wiki.github.org/"));

        return openApi;
    }
}
