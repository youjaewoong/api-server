package api.server.fixedlength.response.common;

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
@Schema(description = "FixedLength 응답")
@NoArgsConstructor
@AllArgsConstructor
public class FixedLengthResponse {

	/** gramId */
	private String gramId;

	/** serviceId */
	private String serviceId;

	/**  헤더 총 길이 */
	private int inHeaderTotal;

	/** 요청 바디 총 길이 */
	private int inBodyTotal;

	/** 응답 바디 데이터 */
	private int outBodyTotal;

	/** 응답 바디 총 길이 */
	private Map<String, String> outFields;

}
