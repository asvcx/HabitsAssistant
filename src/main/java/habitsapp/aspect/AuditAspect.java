package habitsapp.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class AuditAspect {
    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);
    @Pointcut("@annotation(habitsapp.annotations.Auditable)")
    public void annotatedByAuditable() {}

    @Around("annotatedByAuditable()")
    public Object auditUserActions(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        logger.info("User action: {} called with args: {}", methodName, args);
        Object result = joinPoint.proceed();
        logger.info("User action: {} completed", methodName);
        return result;
    }
}
