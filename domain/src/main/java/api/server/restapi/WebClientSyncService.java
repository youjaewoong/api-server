package api.server.restapi;

import api.server.sample.request.SampleRequest;
import api.server.webclient.WebClientSync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebClientSyncService {

	private final WebClientSync webClientSync;

	public String get() {
		return webClientSync.get(1);
	}

	public String post() {
		SampleRequest sampleRequest = new SampleRequest();
		return webClientSync.post(sampleRequest);
	}
}
