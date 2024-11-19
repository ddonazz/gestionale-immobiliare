package it.andrea.start.configuration;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApi30Config {

    private String baseUrl;
    private String baseUrlHttps;

    public OpenApi30Config(Environment environment) {
        baseUrl = environment.getProperty("app.swagger.baseurl");
        baseUrlHttps = environment.getProperty("app.swagger.baseurl-https");
    }

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(Arrays.asList(createServer(baseUrl), createServer(baseUrlHttps)))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .info(new Info().title("Start Spring Project API Documentation").version("v1"));
    }

    private Server createServer(String url) {
        return new Server().url(url);
    }

}
