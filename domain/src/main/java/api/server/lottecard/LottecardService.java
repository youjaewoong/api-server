package api.server.lottecard;

import api.server.restapi.request.RestAPIRequest;
import api.server.sample.request.SampleRequest;
import api.server.webclient.WebClientSync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LottecardService {

	private final WebClientSync webClientSync;

	public String get() {
		return webClientSync.get(1);
	}

	public String post(RestAPIRequest restAPIRequest) {
		return webClientSync.post(restAPIRequest);
	}
}
