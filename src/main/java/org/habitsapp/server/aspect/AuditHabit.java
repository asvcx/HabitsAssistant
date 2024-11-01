package org.habitsapp.server.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.habitsapp.models.Habit;
import org.habitsapp.models.results.HabitCreationResult;
import org.habitsapp.models.dto.HabitDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditHabit {
    private static final Logger logger = LoggerFactory.getLogger(AuditHabit.class);

    @Pointcut("execution(* org.habitsapp.server.service.HabitService.createHabit(..))")
    public void createHabit() {}
    @Around("createHabit()")
    public Object auditCreateHabit(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String email = (String) args[0];
        String habitTitle = ((HabitDto) args[2]).getTitle();
        Object methodResult = joinPoint.proceed();
        if (methodResult instanceof HabitCreationResult result) {
            if (result.getSuccess()) {
                logger.info("User [{}] has created a new habit [{}]", email, habitTitle);
            } else {
                logger.info("User [{}] failed to create a new habit [{}]", email, habitTitle);
            }
        } else {
            logger.info("Method {} returned an object of type: {}", methodName, methodResult.getClass().getSimpleName());
        }
        return methodResult;
    }

    @Pointcut("execution(* org.habitsapp.server.service.HabitService.editHabit(..))")
    public void editHabit() {}
    @Around("editHabit()")
    public Object auditEditHabit(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String email = (String) args[0];
        String habitTitle = ((Habit) args[3]).getTitle();
        Object methodResult = joinPoint.proceed();
        if (methodResult instanceof Boolean result) {
            if (result) {
                logger.info("User [{}] has edited a habit [{}]", email, habitTitle);
            } else {
                logger.info("User [{}] failed to edit a habit [{}]", email, habitTitle);
            }
        } else {
            logger.info("Method {} returned an object of type: {}", methodName, methodResult.getClass().getSimpleName());
        }
        return methodResult;
    }

    @Pointcut("execution(* org.habitsapp.server.service.HabitService.deleteHabit(..))")
    public void deleteHabit() {}
    @Around("deleteHabit()")
    public Object auditDeleteHabit(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String email = (String) args[0];
        String habitTitle = (String) args[2];
        Object methodResult = joinPoint.proceed();
        if (methodResult instanceof Boolean result) {
            if (result) {
                logger.info("User [{}] has deleted a habit [{}]", email, habitTitle);
            } else {
                logger.info("User [{}] failed to delete a habit [{}]", email, habitTitle);
            }
        } else {
            logger.info("Method {} returned an object of type: {}", methodName, methodResult.getClass().getSimpleName());
        }
        return methodResult;
    }

}
