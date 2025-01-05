package api.server.socketserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * <pre>
 * 소켓 서버를 Spring Bean으로 등록
 *  - Spring Boot 애플리케이션이 시작될 때 소켓 서버를 실행.
 *  - @PostConstruct 사용하여 별도의 스레드에서 실행.
 * </pre>
 */
@Profile({"local","dev"})
@Component
@Slf4j
public class FixedLengthSocketServer {
    private static final int PORT = 9090;

    @PostConstruct
    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                log.info("Fixed-Length Socket Server started on port {}", PORT);

                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                        // 요청 전문 수신
                        String request = in.readLine();
                        log.info("Received: {}", request);

                        // 응답 전문 생성
                        String response = String.format("%-10s%-10s%-30s", "홍길동", "22", "test@example.com");

                        // 응답 전송
                        out.println(response);
                    }
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Socket server error: " + e.getMessage(), e);
            }
        }).start(); // 별도 스레드에서 실행
    }
}
