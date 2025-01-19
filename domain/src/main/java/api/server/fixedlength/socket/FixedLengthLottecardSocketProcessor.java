package api.server.fixedlength.socket;

import lombok.extern.slf4j.Slf4j;

/**
 * 롯데카드 소켓통신 클래스.
 * FixedLengthSocketTemplate 확장하여 소켓 통신 구현을 제공합니다.
 */
@Slf4j
public class FixedLengthLottecardSocketProcessor extends FixedLengthSocketTemplate{

    @Override
    protected String sendRequest(String fixedLengthData) {
        // 소켓 통신 로직
        return sendFixedLengthRequest(fixedLengthData);
    }

    // 롯데카드 lib 구성 처리
    private String sendFixedLengthRequest(String fixedLengthData) {
        // 실제 소켓 통신 코드 작성
        return "Response from socket";
    }

}