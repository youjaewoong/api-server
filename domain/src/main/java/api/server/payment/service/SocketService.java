package api.server.payment.service;

import api.server.common.exception.custom.BusinessException;
import api.server.common.helper.BeanHelper;
import api.server.common.properties.EndPointProperties;
import api.server.fixedlength.enmus.FixedLengthErrorCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
@RequiredArgsConstructor
@Slf4j
public class SocketService {

    private final EndPointProperties endPointProperties;

    // 추상 메서드: 서브클래스에서 구현해야 함
    public String sendRequest(String fixedLengthData) {
        // 소켓 통신 로직
        return sendFixedLengthRequest(fixedLengthData);
    }

    private String sendFixedLengthRequest(String request) {

        log.debug("sendFixedLengthRequest: {}", request);
        log.debug("endPointProperties: {}", endPointProperties);

        try (Socket socket = new Socket(endPointProperties.getVan(), endPointProperties.getVanPort());
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
