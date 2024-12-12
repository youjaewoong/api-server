package api.server.sample.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회사정보 응답")
public class SampleCompanyInfoResponse {
	@Schema(description = "회사명", example = "Romaguera-Crona")
	private String name;
	@Schema(description = "문구", example = "Multi-layered client-server neural-net")
	private String catchPhrase;
	@Schema(description = "비즈니스", example = "harness real-time e-markets\"")
	private String bs;
}
