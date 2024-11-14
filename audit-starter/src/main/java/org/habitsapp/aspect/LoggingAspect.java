package org.habitsapp.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(org.habitsapp.annotation.EnableExecMeasurement)")
    public Object auditUserActions(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object methodResult = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;
        String methodName = joinPoint.getSignature().getName();
        logger.info("Method {} executed in: {} ms", methodName, duration);
        return methodResult;
    }
}
