package api.server.webclient;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "WebClient 통신", description = "RestApi 통신을 테스트 합니다.")
@Validated
@RequestMapping("web-client")
public interface WebClientControllerApi {

	@Operation(summary = "sync 목록 정보 조회)",
			description = "sync 샘플 정보를 조회합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = String.class)))
			}
	)
	@GetMapping(value = "sync/get")
	ResponseEntity<String> findWebClientSync();

}
