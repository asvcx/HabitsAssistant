package org.habitsapp.server.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.habitsapp.model.dto.UserDto;
import org.habitsapp.model.result.AuthorizationResult;
import org.habitsapp.model.result.RegistrationResult;
import org.habitsapp.server.repository.AuditRepo;
import org.springframework.beans.factory.annotation.Autowired;

@Aspect
public class AuditUser {
    @Autowired
    private AuditRepo auditRepo;

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
            if (result.success()) {
                Long userId = ((AuthorizationResult) methodResult).userDto().getId();
                auditRepo.saveToLog(userId, String.format("User [%s] has been authorized", email));
            } else {
                auditRepo.saveToLog(null, String.format("User [%s] failed to authorize", email));
            }
        } else {
            String msg = String.format("Method [%s] returned an object of type: [%s]",
                    methodName, methodResult.getClass().getSimpleName());
            auditRepo.saveToLog(null, msg);

        }
        return methodResult;
    }

    @Pointcut("execution(* org.habitsapp.server.service.UserService.registerUser(..))")
    public void registerUser() {}
    @Around("registerUser()")
    public Object auditRegisterUser(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        UserDto userDto = (UserDto) args[0];
        Object methodResult = joinPoint.proceed();
        if (methodResult instanceof RegistrationResult result) {
            if (result.success()) {
                auditRepo.saveToLog(userDto.getId(), String.format("User [%s] has been registered", userDto.getEmail()));
            } else {
                auditRepo.saveToLog(null, String.format("User [%s] failed to register", userDto.getEmail()));
            }
        } else {
            String msg = String.format("Method [%s] returned an object of type: {%s}",
                    methodName, methodResult.getClass().getSimpleName());
            auditRepo.saveToLog(null, msg);
        }
        return methodResult;
    }

    @Pointcut("execution(* org.habitsapp.server.service.UserService.logoutUser(..))")
    public void logoutUser() {}
    @Around("logoutUser()")
    public Object auditLogout(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Long userId = (Long)args[0];
        Object methodResult = joinPoint.proceed();
        if (methodResult instanceof Boolean result) {
            if (result) {
                auditRepo.saveToLog(userId, "User has logged out");
            } else {
                auditRepo.saveToLog(userId, "User failed to log out");
            }
        } else {
            String msg = String.format("Method [%s] returned an object of type: [%s]",
                    methodName, methodResult.getClass().getSimpleName());
            auditRepo.saveToLog(userId, msg);
        }
        return methodResult;
    }

    @Pointcut("execution(* org.habitsapp.server.service.UserService.deleteUser(..))")
    public void deleteUser() {}
    @Around("deleteUser()")
    public Object auditDeleteUser(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        Object methodResult = joinPoint.proceed();
        if (methodResult instanceof Boolean result) {
            if (result) {
                auditRepo.saveToLog(userId, "User [%s] deleted own profile");
            } else {
                auditRepo.saveToLog(userId, "User [%s] failed to delete own profile");
            }
        } else {
            String msg = String.format("Method [%s] returned an object of type: {%s}",
                    methodName, methodResult.getClass().getSimpleName());
            auditRepo.saveToLog(userId, msg);
        }
        return methodResult;
    }

}
