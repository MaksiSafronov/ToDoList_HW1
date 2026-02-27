package com.example.todo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskRepositoryConfig {

    @Bean
    public StubTaskRepository stubTaskRepository() {
        return new StubTaskRepository();
    }
}

