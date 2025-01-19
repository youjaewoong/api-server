package api.server.fixedlength.socket;

import lombok.extern.slf4j.Slf4j;

/**
 * FixedLength 템플릿 클래스.
 * 공통 작업 흐름을 관리하며, 세부 구현은 하위 클래스에서 제공합니다.
 */
@Slf4j
public abstract class FixedLengthSocketTemplate {

    /**
     * 템플릿 메서드: 공통 작업 흐름.
     * 요청 데이터를 처리하여 결과를 반환합니다.
     *
     * @param fixedLengthData FixedLength 요청 데이터.
     * @return 처리된 응답 데이터.
     */
    public final String process(String fixedLengthData) {
        return sendRequest(fixedLengthData);
    }

    // 추상 메서드: 서브클래스에서 구현해야 함
    protected abstract String sendRequest(String fixedLengthData);

}