package api.server.common.config;

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
}
