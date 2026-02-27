package com.example.todo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class TaskLifecycleProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TaskLifecycleProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TaskService || bean instanceof TaskRepository) {
            logger.info("Before initialization of bean '{}' of type {}", beanName, bean.getClass().getName());
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TaskService || bean instanceof TaskRepository) {
            logger.info("After initialization of bean '{}' of type {}", beanName, bean.getClass().getName());
        }
        return bean;
    }
}

