package com.springboot.MyTodoList.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MyTodoList API",
                version = "1.0",
                description = "Backend API for work items, sprints, time entries, app users, analytics, and semantic search."
        )
)
public class OpenApiDocumentationConfig {
}
