package api.server.fixedlength.socket;

import api.server.common.exception.custom.BusinessException;
import api.server.common.helper.BeanHelper;
import api.server.common.properties.EndPointProperties;
import api.server.fixedlength.enmus.FixedLengthErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 소켓통신 테스트 클래스.
 * <pre>
 * FixedLengthSocketTemplate 확장하여 소켓 통신 구현을 제공합니다.
 * 내장소켓을 통해 테스트를 진행 할 수 있습니다.
 * 내장소켓위치 : {@link api.server.socketserver.FixedLengthSocketServer}
 * </pre>
 */
@Slf4j
public class FixedLengthTestSocketProcessor extends FixedLengthSocketTemplate {

    EndPointProperties endPointProperties =
            (EndPointProperties) BeanHelper.getBean("endPointProperties");

    // 추상 메서드: 서브클래스에서 구현해야 함
    @Override
    protected String sendRequest(String fixedLengthData) {
        // 소켓 통신 로직
        return sendFixedLengthRequest(fixedLengthData);
    }

    private String sendFixedLengthRequest(String request) {

        log.debug("sendFixedLengthRequest: {}", request);
        log.debug("endPointProperties: {}", endPointProperties);

        try (Socket socket = new Socket(endPointProperties.getFax(), endPointProperties.getFaxPort());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // 전문 전송
            out.println(request);
            // 전문 응답 수신
            return in.readLine();
        } catch (Exception e) {
            throw new BusinessException(FixedLengthErrorCode.DATA_NOT_FOUND);
        }
    }
}