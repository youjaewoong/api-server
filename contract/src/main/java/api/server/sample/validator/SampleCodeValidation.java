package api.server.sample.validator;

import api.server.exception.custom.BusinessException;
import api.server.exception.enums.ErrorCode;
import api.server.sample.request.DeleteSample;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * SampleRequest 커스텀 예외 처리
 * ["1", "1"] 중복된 값 리스트삭제 시 test
 */
public class SampleCodeValidation implements ConstraintValidator<SampleCode, DeleteSample> {

	@Override
	public boolean isValid(DeleteSample request, ConstraintValidatorContext context) {
		boolean duplicated = request.getIds().stream()
									 .distinct()
									 .count() != request.getIds().size();
		if (duplicated) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
		return true;
	}
}
