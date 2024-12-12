package api.server.common.library;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import api.server.common.exception.InternalServerErrorException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;


public class MultiValueMapConverter {

	private MultiValueMapConverter() {}

	public static MultiValueMap<String, String> convert(ObjectMapper objectMapper, Object dto) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			Map<String, String> map = objectMapper.convertValue(dto, new TypeReference<>() {});
			params.setAll(map);
			return params;
		} catch (IllegalArgumentException illegalArgumentException) {
			throw new InternalServerErrorException("Object to MultiValueMap Convert Error", illegalArgumentException);
		}
	}
}
