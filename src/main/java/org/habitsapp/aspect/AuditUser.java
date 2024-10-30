package org.habitsapp.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.habitsapp.models.dto.UserDto;
import org.habitsapp.models.results.AuthorizationResult;
import org.habitsapp.models.results.RegistrationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class AuditUser {
    private static final Logger logger = LoggerFactory.getLogger(AuditUser.class);

    @Pointcut("execution(* org.habitsapp.server.service.UserService.registerUser(..))")
    public void registerUser() {}
    @Around("registerUser()")
    public Object auditRegisterUser(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String email = ((UserDto) args[0]).getEmail();
        Object methodResult = joinPoint.proceed();
        if (methodResult instanceof RegistrationResult result) {
            if (result.isSuccess()) {
                logger.info("User [{}] has been registered", email);
            } else {
                logger.info("User [{}] failed to register", email);
            }
        } else {
            logger.info("Method {} returned an object of type: {}", methodName, methodResult.getClass().getSimpleName());
        }
        return methodResult;
    }

    @Pointcut("execution(* org.habitsapp.server.service.UserService.authorizeUser(..))")
    public void authorizeUser() {}
    @Around("authorizeUser()")
    public Object auditAuthorize(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String email = (String) args[0];
        Object methodResult = joinPoint.proceed();
        if (methodResult instanceof AuthorizationResult) {
            AuthorizationResult result = (AuthorizationResult) methodResult;
            if (result.isSuccess()) {
                logger.info("User [{}] has been authorized", email);
            } else {
                logger.info("User [{}] failed to authorize", email);
            }
        } else {
            logger.info("Method {} returned an object of type: {}", methodName, methodResult.getClass().getSimpleName());
        }
        return methodResult;
    }

    @Pointcut("execution(* org.habitsapp.server.service.UserService.logoutUser(..))")
    public void logoutUser() {}
    @Around("logoutUser()")
    public Object auditLogout(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Object methodResult = joinPoint.proceed();
        if (methodResult instanceof Boolean result) {
            if (result) {
                logger.info("User has logged out");
            } else {
                logger.info("User failed to log out");
            }
        } else {
            logger.info("Method {} returned an object of type: {}", methodName, methodResult.getClass().getSimpleName());
        }
        return methodResult;
    }

}
