package api.server.sample.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "샘플 로그상태 응답")
public class SampleLogResponse {
	@Schema(description = "debug", example = "true")
	Boolean debug;
	@Schema(description = "info", example = "true")
	Boolean info;
	@Schema(description = "warn", example = "true")
	Boolean warn;
	@Schema(description = "error", example = "true")
	Boolean error;
	@Schema(description = "trace", example = "false")
	Boolean trace;
	@Schema(description = "profile", example = "local")
	String profile;
}
