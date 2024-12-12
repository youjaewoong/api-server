package api.server.sample.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주소정보 응답")
public class SampleAddressInfoResponse {

	@Schema(description = "주소1", example = "Kulas Light")
	private String street;
	@Schema(description = "주소2", example = "Apt. 556")
	private String suite;
	@Schema(description = "주소3", example = "Gwenborough")
	private String city;
	@Schema(description = "우편번호", example = "92998-3874")
	private String zipcode;
	@Schema(description = "위치")
	@Setter
	private SampleGeoResponse geo;
	@Schema(description = "샘플코드종류", example = "['미접수','접수']")
	@Builder.Default
	private List<String> sampleTypeDescriptions = Collections.emptyList();
}
