package com.farmatodo.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI/Swagger para documentación de la API.
 * Define la información de la API del servicio de notificaciones.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configura la documentación OpenAPI del servicio.
     * @return Configuración OpenAPI personalizada
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .version("1.0.0")
                        .description("API para gestión de notificaciones por correo electrónico")
                        .contact(new Contact()
                                .name("Farmatodo")
                                .email("support@farmatodo.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

