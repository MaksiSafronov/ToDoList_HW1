package com.example.todo.config;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope("prototype")
public class PrototypeScopedBean {

    public String generateTaskId() {
        return UUID.randomUUID().toString();
    }
}

