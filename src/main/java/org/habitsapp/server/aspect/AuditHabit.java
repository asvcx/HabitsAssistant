package org.habitsapp.server.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.habitsapp.model.Habit;
import org.habitsapp.model.result.HabitCreationResult;
import org.habitsapp.model.dto.HabitDto;
import org.habitsapp.server.repository.AuditRepo;
import org.springframework.beans.factory.annotation.Autowired;

@Aspect
public class AuditHabit {
    @Autowired
    private AuditRepo auditRepo;

    @Pointcut("execution(* org.habitsapp.server.service.HabitService.createHabit(..))")
    public void createHabit() {}
    @Around("createHabit()")
    public Object auditCreateHabit(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        String habitTitle = ((HabitDto) args[2]).getTitle();
        Object methodResult = joinPoint.proceed();
        if (methodResult instanceof HabitCreationResult result) {
            if (result.success()) {
                auditRepo.saveToLog(userId, String.format("Habit [%s] has been created", habitTitle));
            } else {
                auditRepo.saveToLog(userId, String.format("Failed to create the habit [%s]", habitTitle));
            }
        } else {
            String msg = String.format("Method [%s] returned an object of type: [%s]",
                    methodName, methodResult.getClass().getSimpleName());
            auditRepo.saveToLog(userId, msg);
        }
        return methodResult;
    }

    @Pointcut("execution(* org.habitsapp.server.service.HabitService.editHabit(..))")
    public void editHabit() {}
    @Around("editHabit()")
    public Object auditEditHabit(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        String habitTitle = ((Habit) args[3]).getTitle();
        Object methodResult = joinPoint.proceed();
        if (methodResult instanceof Boolean result) {
            if (result) {
                auditRepo.saveToLog(userId, String.format("Habit [%s] has been changed", habitTitle));
            } else {
                auditRepo.saveToLog(userId, String.format("Failed to change the habit [%s]", habitTitle));
            }
        } else {
            String msg = String.format("Method [%s] returned an object of type: [%s]",
                    methodName, methodResult.getClass().getSimpleName());
            auditRepo.saveToLog(userId, msg);
        }
        return methodResult;
    }

    @Pointcut("execution(* org.habitsapp.server.service.HabitService.deleteHabit(..))")
    public void deleteHabit() {}
    @Around("deleteHabit()")
    public Object auditDeleteHabit(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        String habitTitle = (String) args[2];
        Object methodResult = joinPoint.proceed();
        if (methodResult instanceof Boolean result) {
            if (result) {
                auditRepo.saveToLog(userId, String.format("Habit [%s] has been deleted", habitTitle));
            } else {
                auditRepo.saveToLog(userId, String.format("Failed to delete the habit [%s]", habitTitle));
            }
        } else {
            String msg = String.format("Method [%s] returned an object of type: [%s]",
                    methodName, methodResult.getClass().getSimpleName());
            auditRepo.saveToLog(userId, msg);
        }
        return methodResult;
    }

}
