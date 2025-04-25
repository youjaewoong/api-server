package api.server.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExecutionTimeLoggingAspect {

    private static final String CONTROLLER_LEVEL = "LV1 - Controller";
    private static final String SERVICE_LEVEL = "LV2 - Service";
    private static final String REPOSITORY_LEVEL = "LV3 - Repository";

    @Around("execution(* api.server.*.controller..*(..))")
    public Object logControllerExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint, CONTROLLER_LEVEL);
    }

    @Around("execution(* api.server.*.service..*(..))")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint, SERVICE_LEVEL);
    }

    @Around("execution(* api.server.*.repository..*(..))")
    public Object logRepositoryExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint, REPOSITORY_LEVEL);
    }

    /**
     * 공통 실행 시간 로깅 로직
     */
    private Object logExecutionTime(ProceedingJoinPoint joinPoint, String level) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis(); // 실행 시작 시간

        log.info("[{}] START: {}", level, methodName);
        try {
            Object result = joinPoint.proceed(); // 실제 메서드 실행
            long executionTime = System.currentTimeMillis() - startTime; // 실행 시간 계산
            log.info("[{}] END: {} - Execution time: {} ms", level, methodName, executionTime);
            return result;
        } catch (Throwable throwable) {
            log.error("[{}] ERROR: {}", level, methodName, throwable);
            throw throwable;
        }
    }
}
