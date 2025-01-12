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
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "FixedLength 배치 전문통신", description = "FixedLength 배치 전문통신 IN/OUT 을 구현 합니다.")
@Validated
@RequestMapping("api-server")
public interface FixedLengthBatchControllerApi {

	@Operation(summary = "주로 하나 이상의 File 을 타 시스템에 단방향으로 전송",
			description = "주로 하나 이상의 File 을 타 시스템에 단방향으로 전송하고자 하는 경우 주로 사용",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = FixedLengthResponse.class)))
			}
	)
	@PostMapping(value = "fixed-length/batch/ftp")
	ResponseEntity<Void> procFixedLengthFtp(
			@RequestBody @Schema(description = "FixedLength 요청 데이터", implementation = FixedLengthRequest.class) FixedLengthRequest fixedLengthRequest
	);


	@Operation(summary = "배치 업무계 및 TRIAD 승인계간 FILE 전송",
			description = "업무계 및 TRIAD 와 승인계간 FILE 전송을 위해 NDM 솔루션을 사용하는 경우",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = FixedLengthResponse.class)))
			}
	)
	@PostMapping(value = "fixed-length/batch/ndm")
	ResponseEntity<Void> procFixedLengthNdm(
			@RequestBody @Schema(description = "FixedLength 요청 데이터", implementation = FixedLengthRequest.class) FixedLengthRequest fixedLengthRequest
	);


	@Operation(summary = "시스템내부에서 사용되는 파일을 관리하는 경우",
			description = "인터페이스 되는 파일은 아니지만, 시스템내부에서 사용되는 파일을 관리하는 경우",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = FixedLengthResponse.class)))
			}
	)
	@PostMapping(value = "fixed-length/batch/file")
	ResponseEntity<Void> procFixedLengthFile(
			@RequestBody @Schema(description = "FixedLength 요청 데이터", implementation = FixedLengthRequest.class) FixedLengthRequest fixedLengthRequest
	);

}
