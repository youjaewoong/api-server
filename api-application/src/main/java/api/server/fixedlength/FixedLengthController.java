package api.server.fixedlength;

import api.server.fixedlength.request.FixedLengthRequest;
import api.server.fixedlength.service.FixedLengthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FixedLengthController implements FixedLengthControllerApi {


	private final FixedLengthService fixedLengthService;


	@Override
	public Map<String, String> findFixedLengthData(@RequestBody FixedLengthRequest fixedLengthRequest) throws IOException {
		return fixedLengthService.findFixedLengthData(fixedLengthRequest);
	}
}
