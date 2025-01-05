package api.server.fixedlength.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;
import lombok.Data;

/**
 * 응답 데이터 모델.
 */
@Getter
@Builder
@Schema(description = "FixedLength 응답")
public class FixedLengthResponse implements Serializable {

	private String gramId;
	private String serviceId;
	private int inHeaderTotal;
	private int inBodyTotal;
	private int outBodyTotal;
	private Map<String, String> outFields;


	/**
	 * 응답 데이터를 생성.
	 *
	 * @param gramId           gramId
	 * @param serviceId        serviceId
	 * @param inHeaderTotal    헤더 총 길이
	 * @param inBodyTotal      요청 바디 총 길이
	 * @param outFields        응답 바디 데이터
	 * @param outBodyTotal     응답 바디 총 길이
	 * @return Response 객체
	 */
	public static FixedLengthResponse createResponse(
			String gramId,
			String serviceId,
			int inHeaderTotal,
			int inBodyTotal,
			Map<String, String> outFields,
			int outBodyTotal) {

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
