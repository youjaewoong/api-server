package api.server.sample;

import common.standard.response.GenericCollectionResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SampleController implements SampleControllerApi {


	private final SampleService sampleService;

	@Override
	public ResponseEntity<PageResponse<SampleResponse>> findAllSample(SampleRequest sampleRequest) {

		return ResponseEntity.ok(sampleService.findAllSample(sampleRequest));
	}

	@Override
	public ResponseEntity<ListResponse<SampleResponse>> findSample(SampleRequest sampleRequest) {
		return ResponseEntity.ok(sampleService.findSample(sampleRequest));
	}

	@Override
	public ResponseEntity<SampleResponse> findBySampleId(String id) {
		return ResponseEntity.ok(sampleService.
				findBySampleId(new SampleRequest(id)));
	}

	@Override
	public ResponseEntity<Long> countSample(String id, String title) {

		return ResponseEntity.ok(sampleService.countSample(SampleRequest.builder()
				.id(id)
				.title(title).build()));
	}

	@Override
	public ResponseEntity<Boolean> isBySampleId(String id) {
		return ResponseEntity.ok(sampleService.
				isBySampleId(new SampleRequest(id)));
	}

	@Override
	public ResponseEntity<Void> saveSample(CreateSample createSample) {

		sampleService.saveSample(createSample);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@Override
	public ResponseEntity<Void> modifySample(UpdateSample updateSample) {
		sampleService.modifySample(updateSample);
		return ResponseEntity.ok().build();
	}

	@Override
	public ResponseEntity<Void> removeSample(DeleteSample deleteSample) {
		sampleService.removeSample(deleteSample);
		return ResponseEntity.ok().build();
	}

	@Override
	public ResponseEntity<Void> removeBySampleId(String id) {
		sampleService.removeBySampleId(id);
		return ResponseEntity.ok().build();
	}

	@Override
	public ResponseEntity<ListResponse<SampleFeignResponse>> findSampleFeign() {

		return ResponseEntity.ok(sampleService.findSampleFeign());
	}

	@Override
	public ResponseEntity<Boolean> findSampleSearch() {
		return ResponseEntity.ok(sampleService.findSampleSearch());
	}

	@Override
	public ResponseEntity<Long> stringToLong() {
		return ResponseEntity.ok(sampleService.stringToLong());
	}

	@Override
	public ResponseEntity<GenericCollectionResponse<SampleResponse>> listToList(SampleRequest sampleRequest) {
		return ResponseEntity.ok(sampleService.listToList(sampleRequest));
	}

	@Override
	public ResponseEntity<SampleDetailResponse> infoToDetail() {
		return ResponseEntity.ok(sampleService.infoToDetail());
	}

	@Override
	public ResponseEntity<SampleLogResponse> findBySampleLogsInfo() {
		SampleLogResponse response = SampleLogResponse.builder()
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
