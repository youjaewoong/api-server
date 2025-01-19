package api.server.fixedlength.response.lottecard;

import api.server.fixedlength.response.common.FixedLengthResponse;
import api.server.fixedlength.response.common.ResponseFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 응답 데이터 모델.
 */
@Getter
@Builder
@Schema(description = "TEST001 전문 응답")
@NoArgsConstructor
public class TEST001Response implements ResponseFormatter {

	@Override
	public FixedLengthResponse formatResponse(String gramId,
											  String serviceId,
											  int inHeaderTotal,
											  int inBodyTotal,
											  Map<String, String> outFields,
											  int outBodyTotal) {

		// 포맷팅 처리
		outFields.put("customTest", "이 데이터는 포맷팅 처리를 위한 테스트 입니다.");

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
