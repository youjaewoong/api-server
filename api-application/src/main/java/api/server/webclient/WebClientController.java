package api.server.webclient;

import api.server.restapi.WebClientSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

	@Override
	public ResponseEntity<String> findWebClientPostSync() {

		return ResponseEntity.ok(webClientSyncService.post());
	}
}
