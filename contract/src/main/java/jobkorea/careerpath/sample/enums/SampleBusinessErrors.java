package jobkorea.careerpath.sample.enums;

import java.util.function.BiFunction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import common.standard.exception.business.BusinessErrorCode;
import common.standard.exception.business.BusinessErrorCodeException;

/**
 * 서비스명_시퀀스
 */
@RequiredArgsConstructor
public enum SampleBusinessErrors implements BusinessErrorCode {

	SAMPLE_001("샘플 조회에 실패했습니다.", BusinessErrorCodeException.NOT_FOUND::httpStatus),
	SAMPLE_002("샘플 정보가 존재하지않습니다.", BusinessErrorCodeException.NOT_FOUND::httpStatus),
	SAMPLE_003("샘플 작성에 실패했습니다.", BusinessErrorCodeException.BAD_REQUEST::httpStatus),
	SAMPLE_004("샘플 수정에 실패했습니다.", BusinessErrorCodeException.BAD_REQUEST::httpStatus),
	SAMPLE_005("%s 삭제에 실패했습니다.", BusinessErrorCodeException.BAD_REQUEST::httpStatus);
	@Getter
	private final String message;
	private final BiFunction<BusinessErrorCode, Object[], BusinessErrorCodeException> exception;

	public BusinessErrorCodeException exception(Object... args) {
		return exception.apply(this, args);
	}

	@Override
	public String getErrorCode() {
		return toString();
	}
}
