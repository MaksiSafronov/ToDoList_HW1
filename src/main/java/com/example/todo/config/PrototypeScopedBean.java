package com.example.todo.config;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Prototype-scoped бин, создающийся заново при каждом обращении к контейнеру.
 * Предоставляет генерацию уникальных идентификаторов для задач на основе UUID.
 */
@Component
@Scope("prototype")
public class PrototypeScopedBean {

    public String generateTaskId() {
        return UUID.randomUUID().toString();
    }
}

