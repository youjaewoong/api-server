package api.server.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends RuntimeException {
	
	private static final long serialVersionUID = -2428005905998472032L;

	public InternalServerErrorException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public InternalServerErrorException(String message) {
		super(message);
	}
}
