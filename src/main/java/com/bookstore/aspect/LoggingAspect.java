package com.bookstore.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Pointcut cho tất cả methods trong các class có annotation @Service
     */
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods() {
        // Pointcut for all service methods
    }

    /**
     * Pointcut cho tất cả methods trong package service
     */
    @Pointcut("execution(* com.bookstore.service..*.*(..))")
    public void servicePackageMethods() {
        // Pointcut for all methods in service package
    }

    /**
     * Pointcut kết hợp cả 2 điều kiện
     */
    @Pointcut("serviceMethods() || servicePackageMethods()")
    public void allServiceMethods() {
        // Combined pointcut
    }

    /**
     * Around advice để log method execution time
     */
    @Around("allServiceMethods()")
    public Object logMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // Lấy thông tin method
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        // Tạo StopWatch để đo thời gian
        StopWatch stopWatch = new StopWatch();
        String logPrefix = "[" + className + "." + methodName + "]";

        try {
            // Log trước khi thực hiện method (DEBUG level)
            if (log.isDebugEnabled()) {
                log.debug("{} - START - Arguments: {}", logPrefix, formatArgs(args));
            }

            // Bắt đầu đo thời gian
            stopWatch.start();

            // Thực hiện method
            Object result = joinPoint.proceed();

            // Dừng đo thời gian
            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();

            // Log sau khi thực hiện method (INFO level với thời gian)
            log.info("{} - END - Execution time: {}ms", logPrefix, executionTime);

            // Log chi tiết hơn nếu chạy lâu (> 1000ms)
            if (executionTime > 1000) {
                log.warn("{} - SLOW EXECUTION: {}ms", logPrefix, executionTime);
            }

            return result;

        } catch (Exception e) {
            // Log lỗi nếu có
            log.error("{} - ERROR: {} - {}", logPrefix, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    /**
     * Format arguments để log
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return Arrays.toString(args);
    }
}