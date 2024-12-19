package api.server.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;

import common.standard.enums.GenericEnum;
import common.standard.enums.serializer.GenericEnumJsonSerializer;

/**
 * json config
 */
@Configuration
public class JacksonConfig {
	@Bean
	public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
		return new Jackson2ObjectMapperBuilder()
				.serializerByType(GenericEnum.class, new GenericEnumJsonSerializer())
				.featuresToEnable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
				.featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();

		// Java 8 날짜/시간 모듈 등록
		objectMapper.registerModule(new JavaTimeModule());

		// ISO 8601 형식으로 설정
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		return objectMapper;
	}
}
