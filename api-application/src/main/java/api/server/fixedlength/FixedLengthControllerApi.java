package api.server.fixedlength;

import api.server.fixedlength.request.FixedLengthRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Tag(name = "FixedLength", description = "FixedLength 테스트")
@Validated
public interface FixedLengthControllerApi {

	@Operation(summary = "FixedLength 정보 조회",
			description = "FixedLength 정보를 조회합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = Map.class)))
			}
	)
	@PostMapping(value = "fixed-length/sample")
	Map<String, String> findFixedLengthData(@RequestBody FixedLengthRequest fixedLengthRequest) throws IOException;
}
