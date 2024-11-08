package org.habitsapp.server.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UsersLoading {
    private static final Logger logger = LoggerFactory.getLogger(UsersLoading.class);

    @Pointcut("execution(* org.habitsapp.server.repository.DatabasePostgres.loadUsers(..))")
    public void loadUsers() {}

    @Around("loadUsers()")
    public Object measureUsersLoading(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        logger.info("Calling method {}", proceedingJoinPoint.getSignature());
        long start = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        logger.info("Execution of method {} finished. Execution time is {} ms.",
                proceedingJoinPoint.getSignature(), executionTime);
        return result;
    }
}
