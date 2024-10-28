package org.habitsapp.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.habitsapp.client.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class ExecutionTime {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Pointcut("within(@org.habitsapp.annotations.Measurable *) && execution(* * (..))")
    public void annotatedByMeasurable() {}

    @Around("annotatedByMeasurable()")
    public Object logging(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        logger.info("Calling method {}", proceedingJoinPoint.getSignature());
        long start = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        logger.info("Execution of method {} finished. Execution time is {} ms.",
                proceedingJoinPoint.getSignature(), executionTime);
        return result;
    }
}
