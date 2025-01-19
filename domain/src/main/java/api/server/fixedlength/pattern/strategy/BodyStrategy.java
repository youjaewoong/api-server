package api.server.fixedlength.pattern.strategy;


import api.server.fixedlength.request.FixedLengthRequest;
import api.server.fixedlength.vo.FixedLengthJsonVO;

/**
 * 요청 바디 생성 전략 인터페이스.
 * 다양한 바디 생성 로직을 구현하기 위한 기본 인터페이스입니다.
 */
public interface BodyStrategy {
    /**
     * JSON 모델과 요청 데이터를 기반으로 바디 데이터를 생성합니다.
     *
     * @param jsonModel JSON 데이터 모델.
     * @param request FixedLength 요청 데이터.
     * @return 생성된 바디 문자열.
     */
    String createBody(FixedLengthJsonVO jsonModel, FixedLengthRequest request);
}