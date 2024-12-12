package api.server.sample.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "샘플 상세 정보")
public class SampleDetailResponse {
	@Schema(description = "사용자 정보", implementation = SampleResponse.class)
	private SampleResponse userInformation;
	@Schema(description = "사용자 주소", implementation = SampleAddressInfoResponse.class)
	private SampleAddressInfoResponse addressInformation;

}
