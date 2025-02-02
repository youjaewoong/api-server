package api.server.gramstorage;

import api.server.gramstorage.request.GramInfoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "전문포맷 정의", description = "전문 IN/OUT 포맷정의서를 저장 합니다.")
@Validated
@RequestMapping("api-server/gram-storage")
public interface GramStorageControllerApi {

	@Operation(summary = "엑셀 파일 업로드 및 데이터 저장",
			description = "엑셀 파일 업로드 및 데이터 저장 합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = Map.class)))
			}
	)
	@PostMapping(value = "excel-upload", produces = "application/json", consumes = "multipart/form-data")
	ResponseEntity<String> uploadExcel(
			@RequestParam("file") MultipartFile file,
			@RequestParam("gramId") String gramId,
			@RequestParam("gramName") String gramName,
			@RequestParam("serviceId") String serviceId
	);


	@Operation(summary = "특정 전문 ID로 상세정보 조회",
			description = "특정 전문 ID로 상세정보를 조회 합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = GramInfoRequest.class)))
			}
	)
	@GetMapping("{gramId}")
	ResponseEntity<GramInfoRequest> findGramInfo(@PathVariable String gramId);


	@Operation(summary = "저장된 모든 전문 ID 리스트 조회",
			description = "저장된 모든 전문 ID 리스트 조회 합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = String.class)))
			}
	)
	@GetMapping("list")
	ResponseEntity<List<String>> findAllGramList();
}
