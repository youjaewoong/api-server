package api.server.fixedlength;

import api.server.fixedlength.request.FixedLengthRequest;
import api.server.fixedlength.response.common.FixedLengthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.CompletableFuture;

@Tag(name = "FixedLength 전문통신", description = "FixedLength 전문통신 IN/OUT 을 구현 합니다.")
@Validated
@RequestMapping("api-server")
public interface FixedLengthEAIControllerApi {

	@Operation(summary = "동기 정보 조회",
			description = "소스 시스템의 요청 건에 대해 대상 시스템의 처리 결과를 즉시 받아야 하는 유형",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = FixedLengthResponse.class)))
			}
	)
	@PostMapping(value = "fixed-length/eai/sync")
	ResponseEntity<FixedLengthResponse> findFixedLengthSync (
			@RequestBody @Schema(description = "FixedLength 요청 데이터", implementation = FixedLengthRequest.class) FixedLengthRequest fixedLengthRequest
	);


	@Operation(summary = "비동기 처리 및 조회",
			description = "거래량이 많거나 타겟 시스템의 처리가 오래 걸려서 소스 시스템이 응답을 기다리지 않고 다른 업무를 처리하여야 하는 경우 사용",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = FixedLengthResponse.class)))
			}
	)
	@PostMapping(value = "fixed-length/eai/async")
	ResponseEntity<CompletableFuture<FixedLengthResponse>> findFixedLengthAsync(
			@RequestBody @Schema(description = "FixedLength 요청 데이터", implementation = FixedLengthRequest.class) FixedLengthRequest fixedLengthRequest
	);


	@Operation(summary = "단순 단방향 처리",
			description = "대상 시스템의 응답이 필요 없는 단순 전달 인터페이스 유형",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = FixedLengthResponse.class)))
			}
	)
	@PostMapping(value = "fixed-length/eai/notify")
	void findFixedLengthNotify(
			@RequestBody @Schema(description = "FixedLength 요청 데이터", implementation = FixedLengthRequest.class) FixedLengthRequest fixedLengthRequest
	);


	@Operation(summary = "순서 보장 단방향 처리",
			description = "(응답없음) 전문의 순서가 보장되어야 하고, 순서가 맞지 않을 경우 결번 요청 프로세스가 존재하는 경우 주로 사용",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = FixedLengthResponse.class)))
			}
	)
	@PostMapping(value = "fixed-length/eai/deferred")
	void findFixedLengthDeferred(
			@RequestBody @Schema(description = "FixedLength 요청 데이터", implementation = FixedLengthRequest.class) FixedLengthRequest fixedLengthRequest
	);

}
