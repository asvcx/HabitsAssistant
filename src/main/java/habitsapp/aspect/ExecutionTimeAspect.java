package habitsapp.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class ExecutionTimeAspect {
    @Pointcut("within(@habitsapp.annotations.Measurable *) && execution(* * (..))")
    public void annotatedByMeasurable() {}

    @Around("annotatedByMeasurable()")
    public Object logging(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        System.out.println("Calling method " + proceedingJoinPoint.getSignature());
        long start = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        System.out.println("Execution of method " + proceedingJoinPoint.getSignature() +
                " finished. Execution time is " + executionTime + " ms.");
        return result;
    }
}
