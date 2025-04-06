package api.server.payment.service;

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

    private static final int FIXED_LENGTH = 50; // 고정 메시지 길이
    private final ThreadPoolTaskExecutor socketTaskExecutor;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);


    // 클라이언트로 메시지를 전송
    private void sendMessage(PrintWriter writer, String message) {
        writer.println(formatMessage(message));
    }

    // 클라이언트로부터 메시지를 수신
    private String receiveMessage(BufferedReader reader) {
        try {
            char[] buffer = new char[FIXED_LENGTH];
            if (reader.read(buffer) != -1) {
                return new String(buffer).trim();
            }
        } catch (Exception ex) {
            log.error("Error receiving message: {}", ex.getMessage(), ex);
        }
        return null;
    }

    // 메시지를 고정 길이에 맞게 형식화
    private String formatMessage(String message) {
        if (message.length() > FIXED_LENGTH) {
            return message.substring(0, FIXED_LENGTH); // 초과 시 잘라냄
        }
        return String.format("%-" + FIXED_LENGTH + "s", message); // 부족 시 공백으로 채움
    }

    // 서버 요청 전송 및 응답 수신
    public String sendRequest(String ip, int port, String requestMessage) {
        try (Socket socket = new Socket(ip, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

            log.info("Connecting to {}:{}", ip, port);

            // 요청 전송
            sendMessage(writer, requestMessage);
            log.info("Sent request: {}", requestMessage);

            // 응답 수신
            String response = receiveMessage(reader);
            log.info("Received response: {}", response);

            return response;
        } catch (Exception ex) {
            log.error("Failed to communicate with server: {}", ex.getMessage(), ex);
        }
        return null;
    }

}
