package api.server.fixedlength;

import api.server.fixedlength.request.FixedLengthRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FixedLengthBatchController implements FixedLengthBatchControllerApi {

	@Override
	public ResponseEntity<Void> procFixedLengthFtp(FixedLengthRequest fixedLengthRequest) {
		return null;
	}

	@Override
	public ResponseEntity<Void> procFixedLengthNdm(FixedLengthRequest fixedLengthRequest) {
		return null;
	}

	@Override
	public ResponseEntity<Void> procFixedLengthFile(FixedLengthRequest fixedLengthRequest) {
		return null;
	}
}