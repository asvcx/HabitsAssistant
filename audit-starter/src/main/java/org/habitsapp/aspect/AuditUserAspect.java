package org.habitsapp.aspect;

import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.habitsapp.contract.AuditRepo;

@Aspect
@Setter
public class AuditUserAspect {

    private AuditRepo auditRepo;

    @Pointcut("execution(* *..UserService+.*(..))")
    public void userServiceMethods() {}

    @Around("userServiceMethods()")
    public Object auditUserActions(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Object methodResult = joinPoint.proceed();

        switch (methodName) {

            case "authorizeUser":
                String email = (String) args[0];
                if (methodResult instanceof String result) {
                    if (!result.isEmpty()) {
                        auditRepo.saveToLog(null, String.format("User [%s] has been authorized", email));
                    } else {
                        auditRepo.saveToLog(null, String.format("User [%s] failed to authorize", email));
                    }
                }
                break;

            case "registerUser":
                String user = args[0].toString();
                if (methodResult instanceof Boolean result) {
                    if (result) {
                        auditRepo.saveToLog(null, String.format("User [%s] has been registered", user));
                    } else {
                        auditRepo.saveToLog(null, String.format("User [%s] failed to register", user));
                    }
                }
                break;

            case "logoutUser":
                Long logUserId = (Long) args[0];
                if (methodResult instanceof Boolean result) {
                    auditRepo.saveToLog(logUserId, result ? "User has logged out" : "User failed to log out");
                }
                break;

            case "deleteUser":
                Long delUserId = (Long) args[0];
                if (methodResult instanceof Boolean result) {
                    auditRepo.saveToLog(delUserId, result ? "User deleted own profile" : "User failed to delete own profile");
                }
                break;

            default:
                auditRepo.saveToLog(null, String.format("Unhandled method [%s] returned: [%s]",
                        methodName, methodResult.getClass().getSimpleName()));
        }

        return methodResult;
    }
}
