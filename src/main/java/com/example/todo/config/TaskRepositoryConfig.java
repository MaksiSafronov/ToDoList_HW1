package com.example.todo.config;

import com.example.todo.repository.StubTaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация бинов, связанных с репозиториями задач.
 */
@Configuration
public class TaskRepositoryConfig {

    @Bean
    public StubTaskRepository stubTaskRepository() {
        return new StubTaskRepository();
    }
}

