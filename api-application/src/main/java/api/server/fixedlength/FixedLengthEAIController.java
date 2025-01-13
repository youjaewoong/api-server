package api.server.fixedlength;

import api.server.fixedlength.request.FixedLengthRequest;
import api.server.fixedlength.response.FixedLengthResponse;
import api.server.fixedlength.service.FixedLengthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FixedLengthEAIController implements FixedLengthEAIControllerApi {

	private final FixedLengthService fixedLengthService;


	@Override
	public ResponseEntity<FixedLengthResponse> findFixedLengthSync(FixedLengthRequest fixedLengthRequest) {
		return ResponseEntity.ok(fixedLengthService.findFixedLengthSync(fixedLengthRequest));
	}

	@Override
	public ResponseEntity<CompletableFuture<FixedLengthResponse>> findFixedLengthAsync(FixedLengthRequest fixedLengthRequest) {
		return ResponseEntity.ok(fixedLengthService.findFixedLengthAsync(fixedLengthRequest));
	}

	@Override
	public void findFixedLengthNotify(FixedLengthRequest fixedLengthRequest) {
		fixedLengthService.procFixedLengthNotify(fixedLengthRequest);
	}

	@Override
	public void findFixedLengthDeferred(FixedLengthRequest fixedLengthRequest) {
		fixedLengthService.procFixedLengthDeferred(fixedLengthRequest);
	}
}