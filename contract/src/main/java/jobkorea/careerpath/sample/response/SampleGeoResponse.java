package jobkorea.careerpath.sample.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "위치정보 응답")
public class SampleGeoResponse {
	@Schema(description = "위도", example = "-37.3159")
	private String lat;
	@Schema(description = "경도", example = "81.1496")
	private String lng;
}
