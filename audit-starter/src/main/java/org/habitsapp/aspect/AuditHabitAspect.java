package org.habitsapp.aspect;

import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.habitsapp.contract.AuditRepo;

@Aspect
@Setter
public class AuditHabitAspect {

    private AuditRepo auditRepo;

    @Pointcut(value = "execution(* *..HabitService+.*(..))")
    public void habitServiceMethods() {}

    @Around("habitServiceMethods()")
    public Object auditHabitActions(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        String habitTitle = args[1].toString();

        Object methodResult = joinPoint.proceed();
        Boolean result = (Boolean) methodResult;

        switch (methodName) {
            case "createHabit" -> auditRepo.saveToLog(userId, result
                    ? String.format("Habit [%s] has been created", habitTitle)
                    : String.format("Failed to create the habit [%s]", habitTitle));
            case "editHabit" -> auditRepo.saveToLog(userId, result
                    ? String.format("Habit [%s] has been changed", habitTitle)
                    : String.format("Failed to change the habit [%s]", habitTitle));
            case "deleteHabit" -> auditRepo.saveToLog(userId, result
                    ? String.format("Habit [%s] has been deleted", habitTitle)
                    : String.format("Failed to delete the habit [%s]", habitTitle));
            default -> auditRepo.saveToLog(userId, String.format("Method [%s] returned an object of type: [%s]",
                    methodName, methodResult.getClass().getSimpleName()));
        }

        return methodResult;
    }
}
