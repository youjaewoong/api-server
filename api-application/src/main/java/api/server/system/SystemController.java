package api.server.system;

import api.server.system.response.SystemLogResponse;
import api.server.utils.RequestUtils;
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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "시스템 정보", description = "시스템 정보를 확인합니다.")
@RequestMapping("van-gateway/system")
public class SystemController {


	@Operation(summary = "요청 IP 정보",
			description = "요청 IP 정보를 가져옵니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = Map.class)))
			}
	)
	@GetMapping(value = "client-ip")
	public ResponseEntity<Map<String, String>> findClientIp() {
		Map<String, String> response = new HashMap<>();
		response.put("clientIp", RequestUtils.getClientIp());
		return ResponseEntity.ok(response);
	}


	@Operation(summary = "현재 프로파일의 로그레벨 조회",
			description = "현재 프로파일(local, dev, prod) 중 해당되는 로그레벨을 확인합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = SystemLogResponse.class)))
			}
	)
	@GetMapping(value = "log-level")
	public ResponseEntity<SystemLogResponse> findLogsInfo() {
		SystemLogResponse response = SystemLogResponse.builder()
				.debug(log.isDebugEnabled())
				.info(log.isInfoEnabled())
				.warn(log.isWarnEnabled())
				.error(log.isErrorEnabled())
				.trace(log.isTraceEnabled())
				.profile(System.getProperty("spring.profiles.active"))
				.build();
		return ResponseEntity.ok(response);
	}

}
