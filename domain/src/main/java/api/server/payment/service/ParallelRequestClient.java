package api.server.payment.service;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

@Service
public class ParallelRequestClient {
    private final SocketService socketService;
    private final ThreadPoolTaskExecutor socketTaskExecutor;

    public ParallelRequestClient(SocketService socketService, ThreadPoolTaskExecutor socketTaskExecutor) {
        this.socketService = socketService;
        this.socketTaskExecutor = socketTaskExecutor; // 외부 Bean으로 주입받아 사용
    }

    // 병렬 요청 처리 메서드
    public void sendRequestsInParallel(String ip, int port, int numberOfRequests) {
        CountDownLatch latch = new CountDownLatch(numberOfRequests); // 요청 작업 전체 완료 대기용
        IntStream.range(0, numberOfRequests).forEach(requestId -> {
            // 비동기로 요청 실행
            socketTaskExecutor.execute(() -> {
                try {
                    String request = "Message " + requestId;
                    String response = socketService.sendRequest(ip, port, request);
                    System.out.println("Request #" + requestId + " Response: " + (response != null ? response : "No Response"));
                } catch (Exception e) {
                    System.err.println("Error in request #" + requestId + ": " + e.getMessage());
                } finally {
                    latch.countDown(); // 요청 완료 시 카운터 감소
                }
            });
        });

        // 모든 요청이 완료될 때까지 대기
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for requests to complete.");
        }
    }
}
