package br.com.ml.mktplace.orders.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration
 */
@Configuration
public class OpenApiConfig {
    
    @Value("${app.api.version:1.0}")
    private String apiVersion;
    
    @Value("${server.servlet.context-path:/}")
    private String contextPath;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MercadoLibre Orders API")
                        .description("API para processamento e consulta de pedidos do marketplace")
                        .version(apiVersion)
                        .contact(new Contact()
                                .name("MercadoLibre Engineering")
                                .email("engineering@mercadolibre.com")
                                .url("https://developers.mercadolibre.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://www.mercadolibre.com/terms")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080" + contextPath)
                                .description("Development server"),
                        new Server()
                                .url("https://api-staging.mercadolibre.com" + contextPath)
                                .description("Staging server"),
                        new Server()
                                .url("https://api.mercadolibre.com" + contextPath)
                                .description("Production server")
                ));
    }
}