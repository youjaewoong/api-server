package api.server.webclient;

import api.server.common.model.ListResponse;
import api.server.common.model.PageResponse;
import api.server.sample.request.CreateSample;
import api.server.sample.request.DeleteSample;
import api.server.sample.request.SampleRequest;
import api.server.sample.request.UpdateSample;
import api.server.sample.response.SampleDetailResponse;
import api.server.sample.response.SampleFeignResponse;
import api.server.sample.response.SampleLogResponse;
import api.server.sample.response.SampleResponse;
import common.standard.response.GenericCollectionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
public interface WebClientControllerApi {

	@Operation(summary = "목록 정보 조회(페이지 단위)",
			description = "샘플 정보를 페이지 단위로 조회합니다.",
			responses = {@ApiResponse(responseCode = "200",
					content = @Content(schema = @Schema(implementation = String.class)))
			}
	)
	@GetMapping(value = "web-client/sync/get")
	ResponseEntity<String> findWebClientSync();

}
