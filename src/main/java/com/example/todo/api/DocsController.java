package com.example.todo.api;

import com.example.todo.dto.apiv1.DocsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DocsController {

    private static final String DOCS = """
            Internal API v1 (gateway)
            - POST /api/v1/auth/login — JWT access token
            - GET /api/v1/profile — requires ROLE_USER
            - GET /api/v1/docs — requires READ_PRIVILEGE
            - POST/GET/DELETE /api/v1/tasks — прокси к внешнему сервису задач (RestClient)
            """;

    @GetMapping("/docs")
    public DocsResponse docs() {
        return new DocsResponse("Todo gateway — internal API", DOCS.trim());
    }
}
