package api.server.fixedlength.pattern.strategy;

import api.server.fixedlength.request.FixedLengthRequest;

/**
 * 헤더 생성 전략 인터페이스.
 * 다양한 헤더 생성 로직을 구현하기 위한 기본 인터페이스입니다.
 */
public interface HeaderStrategy {
    /**
     * 요청 데이터를 기반으로 헤더 데이터를 생성합니다.
     *
     * @param request FixedLength 요청 데이터.
     * @return 생성된 헤더 문자열.
     */
    String createHeader(FixedLengthRequest request);
}