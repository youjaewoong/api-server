package api.server.sample.service;

import api.server.webclient.WebClientAsync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebClientAsyncService {

	private final WebClientAsync webClientAsync;

	public String getSync() {
		return webClientAsync.getSync(1);
	}

}
