package api.server.payment.service;

import api.server.common.exception.custom.BusinessException;
import api.server.common.properties.EndPointProperties;
import api.server.fixedlength.enmus.FixedLengthErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;


@Service
@RequiredArgsConstructor
@Slf4j
public class SocketService {

    private final EndPointProperties endPointProperties;
    private final ThreadPoolTaskExecutor socketTaskExecutor; // 스레드 풀 DI

    /**
     * 비동기 방식으로 소켓 통신을 처리
     *
     * @return
     */
    public CompletableFuture<String> sendRequestAsync(String fixedLengthData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String result = sendFixedLengthRequest(fixedLengthData);
                log.info("Response Received: {}", result);
                return result; // 비동기 작업의 결과 반환
            } catch (Exception e) {
                log.error("Error during async request processing: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }, socketTaskExecutor); // 실행을 socketTaskExecutor에서 수행
    }



    /**
     * 블로킹 방식의 소켓 통신 로직 처리
     */
    public String sendFixedLengthRequest(String request) {
        log.debug("sendFixedLengthRequest: {}", request);

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
