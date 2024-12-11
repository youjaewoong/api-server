package jobkorea.careerpath.common.config;

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
import jobkorea.careerpath.common.constant.ProfileConstant;
import lombok.RequiredArgsConstructor;

import common.standard.feign.StandardFeignDecoder;
import common.standard.feign.StandardFeignErrorDecoder;
import common.standard.json.JsonConfigManager;

/**
 * feign default config
 */
@RequiredArgsConstructor
@EnableFeignClients(basePackages = {"jobkorea"})
@Configuration
public class FeignConfig {

	private static final long DEFAULT_CONNECTION_TIMEOUT = 5000L;
	private static final long DEFAULT_READ_TIMEOUT = 1000L * 10L;
	private final JsonConfigManager jsonConfigManager;

	@Bean
	public Encoder feignEncoder() {
		return new SpringEncoder(jsonConfigManager.getObjectFactory());
	}

	@Bean
	public Decoder feignDecoder(ObjectProvider<HttpMessageConverterCustomizer> customizers) {

		return new StandardFeignDecoder(new SpringDecoder(jsonConfigManager.getObjectFactory(), customizers));
	}

	@Bean
	public ErrorDecoder errorDecoder() {
		return new StandardFeignErrorDecoder();
	}

	/**
	 * profile에 따라 log level 설정
	 * @return
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
	 * 연결 및 읽기 시간 설정
	 * @return
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
	 * 재연결 횟수 설정
	 * @return
	 */
	@Bean
	public static Retryer retryer() {
		return Retryer.NEVER_RETRY;
	}

	/**
	 * 공통 request header 추가영역
	 * @return
	 */
	@Bean
	public RequestInterceptor requestInterceptor() {
		return requestTemplate ->
				requestTemplate.header(HttpHeaders.CONTENT_TYPE,
						Collections.singleton(MediaType.APPLICATION_JSON_VALUE));
	}
}
