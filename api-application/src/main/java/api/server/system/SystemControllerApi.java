package api.server.system;

import api.server.system.response.SystemLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "시스템 정보", description = "시스템 정보를 확인합니다.")
@Validated
@RequestMapping("system")
public interface SystemControllerApi {

	@Operation(summary = "요청 IP 정보",
			description = "요청 IP 정보를 가져옵니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = Map.class)))
			}
	)
	@GetMapping(value = "client-ip")
	ResponseEntity<Map<String, String>>  findClientIp();


	@Operation(summary = "현재 프로파일의 로그레벨 조회",
			description = "현재 프로파일(local, dev, prod) 중 해당되는 로그레벨을 확인합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = SystemLogResponse.class)))
			}
	)
	@GetMapping(value = "log-level")
	ResponseEntity<SystemLogResponse> findLogsInfo();


	@Operation(summary = "현재 프로파일의 전문타입의 전문해더공통 조회",
			description = "현재 프로파일의 전문타입의 전문해더공통을 확인합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = Object.class)))
			}
	)
	@GetMapping(value = "common-header")
	ResponseEntity<Object> findCommonHeader();



	@Operation(summary = "현재 전문의 JSON 경로 확인",
			description = "현재 전문의 JSON 경로를 확인합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = String.class)))
			}
	)
	@GetMapping(value = "gram-base-path")
	ResponseEntity<Object> findGramBasePath();
}
