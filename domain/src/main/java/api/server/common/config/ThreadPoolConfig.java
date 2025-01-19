package api.server.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /**
     * <pre>
     * CorePoolSize: 8 × 5 = 40 (코어 수에 I/O 비율을 곱함).
     *
     * 외부 API 호출 또는 소켓 통신에서 일반적으로 발생하는 대기 시간은 약 200ms~500ms 수준입니다.
     * 네트워크 상태와 응답 속도에 따라 달라질 수 있지만, 400ms는 현실적인 예상 값.
     *
     * MaxPoolSize: CorePoolSize 의 2~3배
     * KeepAliveSeconds: 60초 (트래픽 변동성에 따라 조정 가능).
     * CorePoolSize+MaxPoolSize+QueueCapacity=40+100+200=340요청 (최대 340 동시 요청 처리 가능)
     * 동시 요청량 100~200까지 안정적으로 처리 가능.
     * </pre>
     */
    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(40); // 항상 유지되는 기본 스레드 수
        executor.setMaxPoolSize(100); // 최대 생성 가능한 스레드 수
        executor.setQueueCapacity(200); // 대기 요청 수
        executor.setKeepAliveSeconds(60); // 유휴 상태로 유지되는 시간
        executor.setThreadNamePrefix("APIServer-Thread-");
        executor.initialize();
        return executor;
    }
}
