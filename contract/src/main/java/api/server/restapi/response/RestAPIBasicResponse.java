package api.server.restapi.response;

import api.server.restapi.response.common.RestAPIResponse;
import api.server.restapi.response.common.RestAPIResponseFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 응답 데이터 모델.
 */
@Getter
@Builder
@Schema(description = "기본 전문 응답")
@NoArgsConstructor
public class RestAPIBasicResponse implements RestAPIResponseFormatter {

	@Override
	public RestAPIResponse formatResponse(String gramId,
										  String serviceId,
										  Map<String, Object> outFields) {

		// 포맷팅 처리
		return RestAPIResponse
				.builder()
				.gramId(gramId)
				.serviceId(serviceId)
				.outFields(outFields)
				.build();
	}
}
