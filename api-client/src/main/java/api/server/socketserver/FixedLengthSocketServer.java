package api.server.socketserver;

import api.server.common.properties.EndPointProperties;
import lombok.RequiredArgsConstructor;
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
@Profile({"local"})
@Component
@RequiredArgsConstructor
@Slf4j
public class FixedLengthSocketServer {
    private static final int PORT = 9009;

    private final EndPointProperties endPointProperties;

    @PostConstruct
    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(endPointProperties.getFaxPort())) {
                log.info("Fixed-Length Socket Server started on port {}", endPointProperties.getFaxPort());

                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                        // 요청 전문 수신
                        String request = in.readLine();
                        log.info("Received: {}", request);

                        // 응답 전문 생성 TEST001 List 2건
                        String response = String.format("%-200s%-10s%-10s%-30s%-1s%-10s%-10s%-10s%-10s%-10s%-10s",
                                "200  20250208SYSNAME1120000000VOTID 00TEST001 S0120250208170507207192.168.1.1     SERVICE001  123456789987654321OriginalContent     N202301011235450000FSERR001YExtraFieldData                          ",
                                "홍길동", "22", "test@example.com", 2,
                                "테스트1", "테스트2", "테스트3",
                                "테스트4", "테스트5", "테스트6");

                        // 응답 전문 생성 TEST002 List 1건씩
                        /**
                        String response = String.format("%-10s%-10s%-30s%-1s%-10s%-10s%-10s%-1s%-10s%-10s%-10s",
                                "홍길동", "22", "test@example.com", 1,
                                "테스트1", "테스트2", "테스트3", 1,
                                "서브테스트1", "서브테스트2", "서브테스트3");
                        */

                        // 응답 전문 생성 TEST003 단건
                        /**
                        String response = String.format("%-10s%-10s%-30s",
                                "홍길동", "22", "test@example.com");
                         */
                        out.println(response);
                    }
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Socket server error: " + e.getMessage(), e);
            }
        }).start(); // 별도 스레드에서 실행
    }
}
