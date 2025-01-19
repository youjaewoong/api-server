package api.server.fixedlength.response;

import api.server.fixedlength.response.common.FixedLengthResponse;
import api.server.fixedlength.response.common.ResponseFormatter;
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
public class FixedLengthBasicResponse implements ResponseFormatter {

	@Override
	public FixedLengthResponse formatResponse(String gramId,
											  String serviceId,
											  int inHeaderTotal,
											  int inBodyTotal,
											  Map<String, String> outFields,
											  int outBodyTotal) {

		// 포맷팅 처리
		return FixedLengthResponse
				.builder()
				.gramId(gramId)
				.serviceId(serviceId)
				.inHeaderTotal(inHeaderTotal)
				.inBodyTotal(inBodyTotal)
				.outBodyTotal(outBodyTotal)
				.outFields(outFields)
				.build();
	}
}
