package com.example.todo;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.example.todo..*Service.*(..))")
    public Object logAroundServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String methodName = methodSignature.toShortString();
        logger.info("Start method {}", methodName);
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            logger.error("Exception in method {}", methodName, ex);
            throw ex;
        }
        if (methodSignature.getReturnType().equals(Void.TYPE)) {
            logger.info("End method {} with no result", methodName);
        } else {
            logger.info("End method {} with result {}", methodName, result);
        }
        return result;
    }
}

