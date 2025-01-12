package api.server.common.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;


/**
 * MultiValueMapConverter 클래스는 객체를 Spring의 MultiValueMap으로 변환하는 유틸리티를 제공합니다.
 *
 * 주요 기능:
 * - DTO 또는 Map 객체를 MultiValueMap으로 변환.
 * - Spring WebClient 또는 RestTemplate 요청 시 파라미터 형식 변환에 유용.
 *
 * 예외 처리:
 * - 변환 실패 시 Exception을 발생시켜 호출자에게 문제를 알립니다.
 */
@UtilityClass
public class MultiValueMapConverterHelper {

	/**
	 * 주어진 DTO 객체를 MultiValueMap으로 변환합니다.
	 *
	 * @param objectMapper Jackson ObjectMapper 인스턴스
	 * @param dto 변환 대상 DTO 객체
	 * @return MultiValueMap으로 변환된 결과
	 * @throws Exception 변환 실패 시 예외 발생
	 */
	public static MultiValueMap<String, String> convert(ObjectMapper objectMapper, Object dto) throws Exception {
		try {
			// MultiValueMap 초기화
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

			// DTO 객체를 Map<String, String>으로 변환
			Map<String, String> map = objectMapper.convertValue(dto, new TypeReference<>() {});

			// Map 값을 MultiValueMap에 설정
			params.setAll(map);

			return params;
		} catch (IllegalArgumentException illegalArgumentException) {
			// 변환 실패 시 예외 처리
			throw new Exception("Object to MultiValueMap Convert Error", illegalArgumentException);
		}
	}
}