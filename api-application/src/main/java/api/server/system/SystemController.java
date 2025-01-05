package api.server.system;

import api.server.fixedlength.header.HeaderFactory;
import api.server.common.helper.RequestHelper;
import api.server.common.property.GramProperty;
import api.server.system.response.SystemLogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SystemController implements SystemControllerApi {

	private final GramProperty gramProperty;


	@Override
	public ResponseEntity<Map<String, String>> findClientIp() {
		Map<String, String> response = new HashMap<>();
		response.put("clientIp", RequestHelper.getClientIp());
		return ResponseEntity.ok(response);
	}


	@Override
	public ResponseEntity<SystemLogResponse> findLogsInfo() {
		SystemLogResponse response = SystemLogResponse.builder()
				.debug(log.isDebugEnabled())
				.info(log.isInfoEnabled())
				.warn(log.isWarnEnabled())
				.error(log.isErrorEnabled())
				.trace(log.isTraceEnabled())
				.profile(System.getProperty("spring.profiles.active"))
				.build();
		return ResponseEntity.ok(response);
	}


	@Override
	public ResponseEntity<Object> findCommonHeader() {
		String type = gramProperty.getType(); // application.yml의 type 값 읽기
		return ResponseEntity.ok(HeaderFactory.getHeader(type));
	}

}
