package api.server.webclient;

import api.server.restapi.WebClientSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("van-gateway/web-client")
@Tag(name = "WebClient 통신", description = "RestApi 통신을 테스트 합니다.")
public class WebClientController {

	private final WebClientSyncService webClientSyncService;

	@Operation(summary = "sync get 방식의 목록 정보 조회",
			description = "sync get 방식의 샘플 정보를 조회합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = String.class)))
			}
	)
	@GetMapping(value = "sync/get")
	public ResponseEntity<String> findWebClientSync() {

		return ResponseEntity.ok(webClientSyncService.get());
	}

	@Operation(summary = "sync post 방식의 목록 정보 조회",
			description = "sync post 방식의 샘플 정보를 조회합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = String.class)))
			}
	)
	public ResponseEntity<String> findWebClientPostSync() {

		return ResponseEntity.ok(webClientSyncService.post());
	}
}
