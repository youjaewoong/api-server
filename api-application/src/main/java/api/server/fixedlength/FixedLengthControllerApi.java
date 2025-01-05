package api.server.fixedlength;

import api.server.fixedlength.request.FixedLengthRequest;
import api.server.fixedlength.response.FixedLengthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Tag(name = "FixedLength 전문통신", description = "FixedLength 전문통신 IN/OUT 을 구현 합니다.")
@Validated
public interface FixedLengthControllerApi {

	@Operation(summary = "FixedLength 정보 조회",
			description = "FixedLength 정보를 조회합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = FixedLengthResponse.class)))
			}
	)
	@PostMapping(value = "fixed-length/sample")
	ResponseEntity<CompletableFuture<FixedLengthResponse>> findFixedLengthData(
			@RequestBody @Schema(description = "FixedLength 요청 데이터", implementation = FixedLengthRequest.class) FixedLengthRequest fixedLengthRequest
	) throws IOException;

}
