package api.server.socket;

import api.server.common.properties.EndPointProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;


@Component
@RequiredArgsConstructor
@Slf4j
public class SocketClient {


    private final EndPointProperties endPointProperties;
    private final ThreadPoolTaskExecutor socketTaskExecutor;

    /**
     * <pre>
     * 비동기 방식으로 요청 데이터를 서버로 전송하고 응답을 처리합니다.
     *
     * - CompletableFuture를 통해 비동기 작업을 수행하며, 지정된 ThreadPoolTaskExecutor에서 실행됩니다.
     * - 작업 성공 시 서버 응답 데이터를 반환하고, 실패 시 RuntimeException이 발생합니다.
     *
     * 주요 동작 흐름:
     * 1. 요청 데이터를 입력받아 소켓 통신 작업을 비동기적으로 실행합니다.
     * 2. 서버로부터 응답 데이터를 수신 후 결과를 반환하거나, 실패 시 예외를 처리합니다.
     *
     * @param requestData 서버로 전송할 요청 데이터 (고정 길이 문자열).
     * @return CompletableFuture<String> 서버 응답을 포함한 비동기 결과.
     */
    public CompletableFuture<String> sendRequestAsync(String requestData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendFixedLengthRequest(requestData);
            } catch (Exception e) {
                log.error("Error during socket communication: {}", e.getMessage());
                throw new RuntimeException("Socket communication failed", e);
            }
        }, socketTaskExecutor);
    }

    /**
     * <pre>
     * 블로킹 방식 소켓 통신을 통해 요청 데이터를 전송하고 응답 데이터를 수신합니다.
     *
     * @param requestData 서버로 전송할 요청 데이터.
     * @return String 서버로부터 수신된 응답 데이터.
     * @throws Exception 소켓 통신 실패 시 발생하는 예외.
     * </pre>
     */
    public String sendFixedLengthRequest(String requestData) throws Exception {
        log.debug("sendFixedLengthRequest: {}", requestData);
        try (
                Socket socket = new Socket(endPointProperties.getVan(), endPointProperties.getVanPort());
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            writer.println(requestData); // 요청 데이터 전송
            String response = reader.readLine(); // 응답 데이터 수신
            log.info("Response Received: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Failed to process fixed-length request: {}", e.getMessage());
            throw new Exception("Failed to process fixed-length request", e);
        }
    }


}
