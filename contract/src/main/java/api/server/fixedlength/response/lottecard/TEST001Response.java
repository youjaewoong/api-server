package api.server.fixedlength.response.lottecard;

import api.server.common.helper.AmountFormatHelper;
import api.server.common.helper.DateFormatHelper;
import api.server.common.helper.StringFormatHelper;
import api.server.fixedlength.response.common.FixedLengthResponse;
import api.server.fixedlength.response.common.FixedLengthResponseFormatter;
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
@Schema(description = "TEST001 전문 응답")
@NoArgsConstructor
public class TEST001Response implements FixedLengthResponseFormatter {

	@Override
	public FixedLengthResponse formatResponse(String gramId,
											  String serviceId,
											  int inHeaderTotal,
											  int inBodyTotal,
											  Map<String, Object> outFields,
											  int outBodyTotal) {

		// 포맷팅 처리
		outFields.put("Phone1", StringFormatHelper.formatPhoneNumber("01055556666"));
		outFields.put("Phone2", StringFormatHelper.formatPhoneNumber("0105556666"));
		outFields.put("yorN1", StringFormatHelper.getYesOrNo(null));
		outFields.put("yorN2", StringFormatHelper.getYesOrNo(""));

		outFields.put("yorN3", StringFormatHelper.getYesOrNo("Yest"));
		outFields.put("formatAmount1", AmountFormatHelper.formatAmount("100000"));
		outFields.put("formatAmount2", AmountFormatHelper.formatAmount(100000));
		outFields.put("totalSum", AmountFormatHelper.sumStringValues("100","200","300")); // 문자열 합산 테스트

		// 문자열 합산 테스트
		outFields.put("evaluateValue", AmountFormatHelper.evaluateArithmeticExpression("100+200-50*2/5"));

		// 이율 계산 테스트
		outFields.put("calculated", AmountFormatHelper.calculateInterest("1000000", "5.5", "2"));

		// 날짜 포맷 변환 테스트
		outFields.put("formattedDate1", DateFormatHelper.formatDate("20250120", "yyyyMMdd", "YYYY-MM-DD"));
		outFields.put("formattedDate2", DateFormatHelper.formatDate("20250120", "yyyyMMdd", "YYYY.MM.DD"));

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
