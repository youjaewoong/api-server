package jobkorea.careerpath.sample;

import common.standard.response.GenericCollectionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jobkorea.careerpath.common.model.ListResponse;
import jobkorea.careerpath.common.model.PageResponse;
import jobkorea.careerpath.sample.request.CreateSample;
import jobkorea.careerpath.sample.request.DeleteSample;
import jobkorea.careerpath.sample.request.SampleRequest;
import jobkorea.careerpath.sample.request.UpdateSample;
import jobkorea.careerpath.sample.response.SampleDetailResponse;
import jobkorea.careerpath.sample.response.SampleFeignResponse;
import jobkorea.careerpath.sample.response.SampleLogResponse;
import jobkorea.careerpath.sample.response.SampleResponse;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Tag(name = "샘플", description = "샘플 테스트")
@Validated
public interface SampleControllerApi {

	@Operation(summary = "목록 정보 조회(페이지 단위)",
			description = "샘플 정보를 페이지 단위로 조회합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = PageResponse.class)))
			}
	)
	@GetMapping(value = "samples/all")
	ResponseEntity<PageResponse<SampleResponse>> findAllSample(@Valid @ParameterObject SampleRequest sampleRequest);

	@Operation(summary = "목록 정보 조회",
			description = "샘플 리스트를 조회합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = ListResponse.class)))
			}
	)
	@GetMapping(value = "samples")
	ResponseEntity<ListResponse<SampleResponse>> findSample(@Valid @ParameterObject SampleRequest sampleRequest);

	@Operation(summary = "단일 정보 조회",
			description = "id로 샘플 정보를 조회합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = SampleResponse.class)))
			}
	)
	@GetMapping(value = "samples/{id}")
	ResponseEntity<SampleResponse> findBySampleId(
			@Parameter(name = "id", example = "1", description = "고유id", in = ParameterIn.PATH)
			@PathVariable
			@Valid @Min(0) @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력해주세요.")
			String id);

	@Operation(summary = "목록 정보 카운트",
			description = "목록 정보의 카운터를 조회합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = Long.class)))
			}
	)
	@GetMapping(value = "samples/total")
	ResponseEntity<Long> countSample(@Valid
									 @Parameter(description = "고유ID", example = "", name = "id")
									 @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력해주세요.")
									 @RequestParam(value = "id", required = false) String id,
									 @Parameter(description = "제목", example = "", name = "title", required = false)
									 @Size(min = 0, max = 20)
									 @RequestParam(value = "title", required = false) String title
	);

	@Operation(summary = "정보 확인",
			description = "id로 샘플 정보가 존재하는지 확인합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = Boolean.class)))
			}
	)
	@GetMapping(value = "samples/{id}/check")
	ResponseEntity<Boolean> isBySampleId(
			@Parameter(name = "id", example = "1", description = "고유id", in = ParameterIn.PATH)
			@PathVariable
			@Valid @Min(0) @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력해주세요.")
			String id);

	@Operation(summary = "정보 등록",
			description = "샘플 정보를 등록합니다.")
	@PostMapping(value = "samples")
	ResponseEntity<Void> saveSample(@Valid @RequestBody CreateSample createSample);

	@Operation(summary = "정보 수정",
			description = "샘플 정보를 수정합니다.")
	@PutMapping("samples")
	ResponseEntity<Void> modifySample(@Valid @RequestBody UpdateSample updateSample);

	@Operation(summary = "여러 정보 삭제",
			description = "id 리스트로 샘플 정보를 삭제합니다.")
	@DeleteMapping("samples")
	ResponseEntity<Void> removeSample(@Valid @RequestBody DeleteSample deleteSample);

	@Operation(summary = "단일 정보 삭제",
			description = "id로 샘플 정보를 삭제합니다.")
	@DeleteMapping("samples/{id}")
	ResponseEntity<Void> removeBySampleId(
			@Parameter(name = "id", description = "고유id", in = ParameterIn.PATH)
			@PathVariable
			@Valid @Min(0) @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력해주세요.")
			String id);

	@Operation(summary = "외부데이터 조회(페인)",
			description = "OpenFeign을 통해 http://jsonplaceholder.typicode.com/users 정보를 조회합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = SampleFeignResponse.class)))
			}
	)
	@GetMapping(value = "samples/users")
	ResponseEntity<ListResponse<SampleFeignResponse>> findSampleFeign();

	@Operation(summary = "검색 조회",
			description = "검색 API를 통해 정보를 조회합니다.(console log로 출력됩니다.")
	@GetMapping(value = "samples/search")
	ResponseEntity<Boolean> findSampleSearch();



	@Operation(summary = "String 을 Long 으로 변환",
			description = "Mapstruct 로 String 을 Long 으로 변환 합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = Long.class)))
			}
	)
	@GetMapping(value = "samples/mapstruct/string-to-long")
	ResponseEntity<Long> stringToLong();

	@Operation(summary = "entityList 를 responseList 변환",
			description = "Mapstruct 로 entityList 를 responseList 변환 합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = GenericCollectionResponse.class)))
			}
	)
	@GetMapping(value = "samples/mapstruct/list-to-list")
	ResponseEntity<GenericCollectionResponse<SampleResponse>> listToList(@Valid @ParameterObject SampleRequest sampleRequest);

	@Operation(summary = "정보 변환 및 상세정보 추가",
			description = "Mapstruct 로 entity 를 response 변환 및 상세정보를 추가합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = SampleDetailResponse.class)))
			}
	)
	@GetMapping(value = "samples/mapstruct/info-to-detail")
	ResponseEntity<SampleDetailResponse> infoToDetail();

	@Operation(summary = "현재 프로파일의 로그레벨 조회",
			description = "현재 프로파일(local, dev, prod) 중 해당되는 로그레벨을 확인합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = SampleLogResponse.class)))
			}
	)
	@GetMapping(value = "samples/log-level")
	ResponseEntity<SampleLogResponse> findBySampleLogsInfo();

}
