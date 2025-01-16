package api.server.common.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 비즈니스 로직 실행 단계별 Time 체크 및 실행 메소드를 확인 합니다.
 */
@Aspect
@Component
@Slf4j
public class ExecutionTimeLoggingAspect {

    /**
     * Controller 레벨 로그 (LV1)
     */
    @Around("execution(* api.server.*.controller..*(..))")
    public Object logControllerExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint, "LV1 - Controller");
    }

    /**
     * Service 레벨 로그 (LV2)
     */
    @Around("execution(* api.server.*.service..*(..))")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint, "LV2 - Service");
    }

    /**
     * Repository 레벨 로그 (LV3)
     */
    @Around("execution(* api.server.*.repository..*(..))")
    public Object logRepositoryExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint, "LV3 - Repository");
    }

    /**
     * 공통 실행 시간 로깅 로직
     */
    private Object logExecutionTime(ProceedingJoinPoint joinPoint, String level) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis(); // 시작 시간 기록

        log.info("[{}] START: {}", level, methodName);
        Object result;

        try {
            result = joinPoint.proceed(); // 실제 메서드 실행
        } catch (Throwable throwable) {
            log.error("[{}] ERROR: {}", level, methodName, throwable);
            throw throwable;
        }

        long endTime = System.currentTimeMillis(); // 종료 시간 기록
        long executionTime = endTime - startTime;

        log.info("[{}] END: {} - Execution time: {} ms", level, methodName, executionTime);
        return result;
    }
}
