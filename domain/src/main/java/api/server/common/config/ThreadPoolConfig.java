package api.server.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;


/**
 * ThreadPool 설정 클래스
 *
 * </pre>
 * 이 클래스는 I/O 중심의 비동기 작업을 처리하기 위해 `ThreadPoolTaskExecutor`를 설정합니다.
 * AWS EC2 `m6i.large` (2 vCPU, 8GB RAM) 환경을 기준으로 최적화된 설정입니다.
 * </pre>
 *
 * 주요 특징
 * <pre>
 * 1. **CorePoolSize = 10** → 기본적으로 유지되는 스레드 개수 (2 vCPU의 5배)
 * 2. **MaxPoolSize = 20** → 최대 생성 가능한 스레드 개수 (트래픽 증가 시 확장 가능)
 * 3. **QueueCapacity = 100** → 최대 100개의 작업을 대기열에서 기다릴 수 있음
 * 4. **KeepAliveSeconds = 30** → 유휴 상태의 스레드는 30초 후 종료됨
 * 5. **RejectedExecutionHandler = CallerRunsPolicy** → 대기열이 가득 차면 호출한 쓰레드에서 직접 실행
 * </pre>
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /**
     * 비동기 작업을 위한 ThreadPoolTaskExecutor 설정
     *
     * @return 설정된 ThreadPoolTaskExecutor 객체
     */
    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        /**
         *  Core Pool Size (기본 스레드 개수)
         *
         * - 기본적으로 실행될 스레드 개수를 지정합니다.
         * - `m6i.large`는 2 vCPU이므로, 보통 `CPU 개수 * 5` 정도로 설정 (I/O 작업이 많을 경우)
         * - 최소 `10`개의 스레드가 항상 유지되며, 요청이 없더라도 유지됨.
         */
        executor.setCorePoolSize(10);

        /**
         *  Max Pool Size (최대 스레드 개수)
         *
         * - 요청이 급증할 경우, 최대 `20`개까지 스레드를 확장할 수 있음.
         * - 하지만, QueueCapacity가 가득 차지 않으면 MaxPoolSize까지 확장되지 않음.
         * - 동시 요청이 많다면 MaxPoolSize를 높게 설정 가능하지만, CPU 사용량 고려 필요.
         */
        executor.setMaxPoolSize(20);

        /**
         *  Queue Capacity (대기열 크기)
         *
         * - CorePoolSize(10개 스레드)가 모두 사용 중일 때, 추가 요청을 대기열에 저장하는 공간.
         * - 최대 100개의 요청을 저장할 수 있으며, 초과 시 RejectedExecutionHandler가 동작함.
         * - I/O Bound 작업이므로 큐 크기를 충분히 설정하여 트래픽 급증 시 대기 가능하도록 함.
         */
        executor.setQueueCapacity(100);

        /**
         *  Keep Alive Seconds (유휴 스레드 유지 시간)
         *
         * - CorePoolSize를 초과하여 생성된 스레드는 일정 시간이 지나면 제거됨.
         * - 30초 동안 추가 생성된 스레드가 유휴 상태이면 자동으로 종료됨.
         * - CorePoolSize 이하의 스레드는 기본적으로 유지됨.
         */
        executor.setKeepAliveSeconds(30);

        /**
         *  Thread Name Prefix (스레드 명 설정)
         *
         * - 생성되는 스레드의 이름을 설정하여 로깅 및 디버깅 시 유용하게 활용 가능.
         * - "APIServer-IO-Thread-" 접두사를 사용하여 각 스레드를 쉽게 식별할 수 있도록 함.
         */
        executor.setThreadNamePrefix("APIServer-IO-Thread-");

        /**
         *  Rejected Execution Handler (대기열 초과 시 처리 방식)
         *
         * - 대기열(Queue)이 가득 차고, MaxPoolSize의 모든 스레드가 사용 중일 때 실행할 정책.
         * - `CallerRunsPolicy` 사용 시, 요청을 보낸 쓰레드(main 쓰레드)가 직접 해당 작업을 실행함.
         * - 서버 과부하 방지 및 요청 손실 방지를 위해 설정함.
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        /**
         *  ThreadPoolTaskExecutor 초기화
         *
         * - 위에서 설정한 값들을 기반으로 실행을 준비함.
         * - 이 메서드를 호출하지 않으면 설정이 적용되지 않을 수 있음.
         */
        executor.initialize();

        return executor;
    }
}
