package api.server.webclient;

import api.server.sample.request.SampleRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientSync {

	private final WebClient webClient;

	// WebClient 주입 (생성자를 통한 DI)
	public WebClientSync(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl("https://jsonplaceholder.typicode.com").build();
	}


	// 동기 호출 메서드
	public String get(int postId) {
		// GET 요청을 보내고, block()으로 결과를 동기적으로 반환
		return webClient.get()
				.uri("/posts/{id}", postId) // URI에 파라미터 바인딩
				.retrieve()                // HTTP 응답을 처리
				.bodyToMono(String.class)  // 응답을 String 타입으로 변환
				.block();                  // 동기적으로 결과 반환
	}


	// 동기 POST 호출
	public String post(SampleRequest sampleRequest) {
		// POST 요청 생성
		return webClient.post()
				.uri("/posts")          // API 경로
				.bodyValue(sampleRequest)	// 요청 본문 설정 (JSON 변환 자동 처리)
				.retrieve()                 // 응답 처리 시작
				.bodyToMono(String.class)   // 응답을 String으로 변환
				.block();                   // 동기 방식으로 결과 대기 및 반환
	}
}