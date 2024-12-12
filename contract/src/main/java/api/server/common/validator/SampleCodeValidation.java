package api.server.common.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import api.server.sample.request.DeleteSample;

import common.standard.exception.business.BusinessErrorCodeExceptionStrictMessage;

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
			throw BusinessErrorCodeExceptionStrictMessage.badRequest("중복된 ID가 존재합니다.");
		}
		return true;
	}
}
