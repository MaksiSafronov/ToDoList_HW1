package com.example.todo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI todoListOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("To-Do List API")
                        .version("2.0.0")
                        .description("REST API для управления задачами: CRUD задач, вложения файлов, "
                                + "избранное в сессии и пользовательские настройки (куки). "
                                + "Заголовки ответа включают X-API-Version и при списке задач — X-Total-Count.")
                        .contact(new Contact()
                                .name("To-Do API Team")
                                .email("api-support@example.com")
                                .url("https://example.com/support")));
    }
}
