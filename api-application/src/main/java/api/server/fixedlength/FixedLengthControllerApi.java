package api.server.fixedlength;

import api.server.fixedlength.request.FixedLengthRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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


	@Operation(summary = "FixedLength 헤더 Excel 파일 업로드",
			description = "Excel 파일을 업로드하여 공통 헤더를 저장합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = String.class)))
			}
	)
	@PostMapping(value = "fixed-length/excel/upload/common-header", produces = "application/json", consumes = "multipart/form-data")
	String uploadFixedLengthCommonHeader(@RequestParam("file") MultipartFile file);


	@Operation(summary = "FixedLength  헤더 조회",
			description = "저장된 공통 헤더 데이터를 반환합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = Map.class)))
			}
	)
	@GetMapping(value = "fixed-length/excel/get/common-header")
	Map<String, Object> findFixedLengthCommonHeader();
}
