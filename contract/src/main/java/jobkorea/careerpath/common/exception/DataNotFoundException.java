package jobkorea.careerpath.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Subdomain/BFF 에 한정하여 사용되는 Exception 의 경우 Application-Side 에 작성 될 수 있습니다.
 * 단, 일반적인 Exception 일 경우 공통 Library 로 이관하거나, Java Embed Exception 을 사용하는 것을 고려해야 합니다.
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class DataNotFoundException extends RuntimeException {

	private
	static final long serialVersionUID = -4057077883987423224L;

	public DataNotFoundException(String message) {
		super(message);
	}

	public DataNotFoundException(String message, Throwable throwable) {
		super(message, throwable);
	}
}