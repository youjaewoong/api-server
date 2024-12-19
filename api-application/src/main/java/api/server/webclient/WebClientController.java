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
import api.server.sample.service.SampleService;
import api.server.sample.service.WebClientSyncService;
import common.standard.response.GenericCollectionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WebClientController implements WebClientControllerApi {

	private final WebClientSyncService webClientSyncService;


	@Override
	public ResponseEntity<String> findWebClientSync() {
		return ResponseEntity.ok(webClientSyncService.get());
	}
}
