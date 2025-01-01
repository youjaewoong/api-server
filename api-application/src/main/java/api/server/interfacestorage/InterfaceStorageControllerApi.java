package api.server.interfacestorage;

import api.server.fixedlength.request.FixedLengthRequest;
import api.server.interfacestorage.request.InterfaceInfo;
import api.server.interfacestorage.service.InterfaceParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "interface-storage", description = "전문 IN/OUT 포맷정의서를 저장 합니다.")
@Validated
public interface InterfaceStorageControllerApi {

	@Operation(summary = "엑셀 파일 업로드 및 데이터 저장",
			description = "엑셀 파일 업로드 및 데이터 저장 합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = Map.class)))
			}
	)
	@PostMapping(value = "interface-storage/upload", produces = "application/json", consumes = "multipart/form-data")
	ResponseEntity<String> uploadExcel(
			@RequestParam("file") MultipartFile file,
			@RequestParam("interfaceId") String interfaceId,
			@RequestParam("interfaceName") String interfaceName
	);


	@Operation(summary = "특정 인터페이스 ID로 데이터 조회",
			description = "특정 인터페이스 ID로 데이터 조회 합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = InterfaceInfo.class)))
			}
	)
	@GetMapping("interface-storage/{interfaceId}")
	ResponseEntity<InterfaceInfo> getInterface(@PathVariable String interfaceId);


	@Operation(summary = "저장된 모든 인터페이스 ID 리스트 조회",
			description = "저장된 모든 인터페이스 ID 리스트 조회 합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = String.class)))
			}
	)
	@GetMapping("interface-storage/list")
	ResponseEntity<List<String>> listAllInterfaceIds();
}
