package api.server.common.config;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import api.server.common.constant.ProfileConstant;
import lombok.RequiredArgsConstructor;

import common.standard.feign.StandardFeignDecoder;
import common.standard.feign.StandardFeignErrorDecoder;
import common.standard.json.JsonConfigManager;
import org.springframework.http.converter.StringHttpMessageConverter;

/**
 * feign default config
 */
@RequiredArgsConstructor
@EnableFeignClients(basePackages = {"api"})
@Configuration
public class FeignConfig {

	private static final long DEFAULT_CONNECTION_TIMEOUT = 5000L;
	private static final long DEFAULT_READ_TIMEOUT = 1000L * 10L;
	private final JsonConfigManager jsonConfigManager;

	/**
	 * Feign 요청을 위한 Encoder를 설정합니다.
	 *
	 * @return Feign Encoder
	 */
	@Bean
	public Encoder feignEncoder() {
		return new SpringEncoder(jsonConfigManager.getObjectFactory());
	}

	/**
	 * Feign 응답을 위한 Decoder를 설정하며, UTF-8 인코딩을 지원합니다.
	 *
	 * @param customizers HTTP Message Converter Customizer
	 * @return Feign Decoder
	 */
	@Bean
	public Decoder feignDecoder(ObjectProvider<HttpMessageConverterCustomizer> customizers) {
		return new StandardFeignDecoder(new SpringDecoder(jsonConfigManager.getObjectFactory(), customizers));
	}

	/**
	 * 커스텀 ErrorDecoder를 설정합니다.
	 *
	 * @return Feign ErrorDecoder
	 */
	@Bean
	public ErrorDecoder errorDecoder() {
		return new StandardFeignErrorDecoder();
	}

	/**
	 * 프로파일에 따라 Feign의 로그 레벨을 설정합니다.
	 *
	 * @return Feign 로그 레벨
	 */
	@Bean
	public static Logger.Level logLevel() {
		String profile = System.getProperty("spring.profiles.active");
		if (ProfileConstant.LOCAL.equals(profile)) {
			return Logger.Level.FULL;
		}
		return Logger.Level.BASIC;
	}

	/**
	 * Feign 요청의 연결 및 읽기 시간 초과 설정을 정의합니다.
	 *
	 * @return Feign 요청 옵션
	 */
	@Bean
	public static Request.Options options() {
		return new Request.Options(DEFAULT_CONNECTION_TIMEOUT,
				TimeUnit.MILLISECONDS,
				DEFAULT_READ_TIMEOUT,
				TimeUnit.MILLISECONDS,
				false);
	}

	/**
	 * Feign 재시도 설정 (비활성화).
	 *
	 * @return Feign Retryer
	 */
	@Bean
	public static Retryer retryer() {
		return Retryer.NEVER_RETRY;
	}

	/**
	 * Feign 요청에 공통 헤더(Content-Type)를 추가합니다.
	 *
	 * @return Feign RequestInterceptor
	 */
	@Bean
	public RequestInterceptor requestInterceptor() {
		return requestTemplate ->
				requestTemplate.header(HttpHeaders.CONTENT_TYPE,
						Collections.singleton(MediaType.APPLICATION_JSON_VALUE));
	}

	/**
	 * Feign 클라이언트에서 UTF-8 응답 처리를 위해 StringHttpMessageConverter를 UTF-8로 설정합니다.
	 *
	 * @return HTTP Message Converter Customizer
	 */
	@Bean
	public HttpMessageConverterCustomizer utf8MessageConverterCustomizer() {
		return converters -> converters.stream()
				.filter(StringHttpMessageConverter.class::isInstance)
				.map(StringHttpMessageConverter.class::cast)
				.forEach(converter -> converter.setDefaultCharset(java.nio.charset.StandardCharsets.UTF_8));
	}
}
