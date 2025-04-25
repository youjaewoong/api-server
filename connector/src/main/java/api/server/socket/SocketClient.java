package api.server.socket;

import api.server.properties.EndPointProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
        log.info("getVan: {}", endPointProperties.getVan());
        log.info("getVanPort: {}", endPointProperties.getVanPort());

        try (
                Socket socket = new Socket(endPointProperties.getVan(), endPointProperties.getVanPort())
        ) {
            if ("localhost".equals(endPointProperties.getVan())) {
                // 로컬 테스트: 텍스트 라인 기반 통신
                try (
                        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                ) {
                    writer.println(requestData); // 요청 데이터 전송
                    String response = reader.readLine(); // 응답 데이터 수신
                    log.info("Response Received (text): {}", response);
                    return response;
                }
            } else {
                // 실제 VAN 연동: 바이트 기반 통신
                log.info("[VAN MODE] 실서버 연동 통신 방식 진입");

                try (
                        OutputStream output = socket.getOutputStream();
                        InputStream input = socket.getInputStream()
                ) {
                    // 요청
                    byte[] requestBytes = requestData.getBytes("EUC-KR");
                    output.write(requestBytes);
                    output.flush();
                    log.info("[VAN MODE] 요청 전송 완료: {}", requestData);
                    log.info("[VAN MODE] 요청 바이트 (Hex): {}", bytesToHex(requestBytes));

                    // 응답 길이 읽기 (앞 4바이트)
                    byte[] lengthBytes = new byte[4];
                    int lenRead = input.read(lengthBytes);
                    if (lenRead != 4) {
                        throw new IOException("응답 길이 바이트(4자리) 읽기 실패. read=" + lenRead);
                    }

                    String lengthStr = new String(lengthBytes, StandardCharsets.US_ASCII);
                    int bodyLength = Integer.parseInt(lengthStr);
                    log.info("[VAN MODE] 수신할 응답 바디 길이: {} bytes", bodyLength);

                    // 응답 바디 읽기
                    byte[] bodyBytes = new byte[bodyLength];
                    int totalRead = 0;
                    while (totalRead < bodyLength) {
                        int read = input.read(bodyBytes, totalRead, bodyLength - totalRead);
                        if (read == -1) {
                            throw new IOException("응답 바디 수신 중 연결 종료됨");
                        }
                        totalRead += read;
                    }

                    // 전체 응답 = 길이 + 바디
                    byte[] responseBytes = new byte[4 + bodyLength];
                    System.arraycopy(lengthBytes, 0, responseBytes, 0, 4);
                    System.arraycopy(bodyBytes, 0, responseBytes, 4, bodyLength);

                    // Hex 로그
                    log.info("[VAN MODE] 전체 응답 바이트 (Hex): {}", bytesToHex(responseBytes));

                    // 디코딩 결과 로그
                    try {
                        String decodedEucKr = new String(bodyBytes, "EUC-KR");
                        log.info("[VAN MODE] 응답 디코딩 [EUC-KR]: {}", decodedEucKr);
                    } catch (Exception e) {
                        log.warn("[VAN MODE] EUC-KR 디코딩 실패: {}", e.getMessage());
                    }

                    // 운영용 반환 (EUC-KR 기준)
                    return new String(bodyBytes, "EUC-KR");

                } catch (Exception e) {
                    log.error("VAN 통신 실패: {}", e.getMessage(), e);
                    throw new Exception("Failed to process fixed-length request", e);
                }

            }
        } catch (Exception e) {
            log.error("Failed to process fixed-length request: {}", e.getMessage());
            throw new Exception("Failed to process fixed-length request", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

}
