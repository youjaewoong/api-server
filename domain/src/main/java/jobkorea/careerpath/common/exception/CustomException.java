package jobkorea.careerpath.common.exception;

import common.standard.exception.controller.DefaultControllerValidationExceptionHandle;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomException extends DefaultControllerValidationExceptionHandle {
}
