package api.server.common.config;

import api.server.common.properties.EndPointProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * WebClient 설정을 구성하는 클래스.
 * WebClient를 Bean으로 등록하여 애플리케이션 전반에서 재사용할 수 있도록 설정한다.
 * - 기본 URL 설정
 * - 타임아웃 및 네트워크 설정
 * - 요청 및 응답 로깅 필터 추가
 * </pre>
 */
@Configuration
@RequiredArgsConstructor
@DependsOn("endPointProperties")  // EndPointProperties가 먼저 로드되도록 설정
@Slf4j
public class WebClientConfig {

    private final EndPointProperties endPointProperties;

    /**
     * <pre>
     * WebClient Bean 을 생성하여 반환한다.
     * - 기본 요청 URL 설정
     * - 연결 및 응답 타임아웃 설정
     * - 요청 및 응답 로깅 필터 적용
     *
     * @return 설정된 WebClient 인스턴스
     * </pre>
     */
    @Bean
    public WebClient webClient() {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 연결 타임아웃 (5초)
                .responseTimeout(Duration.ofMillis(5000)) // 응답 타임아웃 (5초)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                );

        return WebClient.builder()
                .baseUrl(endPointProperties.getApi()) // 기본 URL 설정
                .clientConnector(new ReactorClientHttpConnector(httpClient)) // Reactor Netty 설정 적용
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE) // Accept 헤더 상수 사용
                .filter(logRequest()) // 요청 로깅 필터 추가
                .filter(logResponse()) // 응답 로깅 필터 추가
                .build();
    }


    /**
     * <pre>
     * 요청을 로깅하는 필터.
     * HTTP 메서드와 요청 URL을 출력한다.
     *
     * @return 요청 로깅 필터
     * </pre>
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            return reactor.core.publisher.Mono.just(clientRequest);
        });
    }


    /**
     * <pre>
     * 응답을 로깅하는 필터.
     * 응답 상태 코드를 출력한다.
     *
     * @return 응답 로깅 필터
     * </pre>
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response status: {}", clientResponse.statusCode());
            return reactor.core.publisher.Mono.just(clientResponse);
        });
    }
}