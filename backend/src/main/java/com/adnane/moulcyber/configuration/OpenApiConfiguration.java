package com.adnane.moulcyber.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI moulCyberOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Moul Cyber API")
                        .version("0.0.1")
                        .description("Video game rental and inventory management API."))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .addTagsItem(new Tag().name("Health").description("Application health endpoint."))
                .addTagsItem(new Tag().name("Authentication").description("Registration and login endpoints."))
                .addTagsItem(new Tag().name("Catalog").description("Public game catalog browsing endpoints."))
                .addTagsItem(new Tag().name("Rentals").description("Authenticated rental creation and customer rental history."))
                .addTagsItem(new Tag().name("Reviews").description("Public review reading and authenticated review creation."))
                .addTagsItem(new Tag().name("Users").description("Authenticated current user profile endpoints."))
                .addTagsItem(new Tag().name("Admin Catalog").description("Administrative catalog and inventory management endpoints."))
                .addTagsItem(new Tag().name("Admin Rentals").description("Administrative rental monitoring endpoints."))
                .addTagsItem(new Tag().name("Admin Rental Items").description("Administrative rental item processing endpoints."));
    }
}
