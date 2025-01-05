package api.server.fixedlength;

import api.server.fixedlength.request.FixedLengthRequest;
import api.server.fixedlength.response.FixedLengthResponse;
import api.server.fixedlength.service.FixedLengthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FixedLengthController implements FixedLengthControllerApi {

	private final FixedLengthService fixedLengthService;

	@Override
	public ResponseEntity<CompletableFuture<FixedLengthResponse>> findFixedLengthData(@RequestBody FixedLengthRequest fixedLengthRequest) throws IOException {
		return ResponseEntity.ok(fixedLengthService.findFixedLengthData(fixedLengthRequest));
	}
}