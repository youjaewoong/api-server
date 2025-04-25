package api.server.webclient;

import api.server.sample.request.SampleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class WebClientAsync {

	private final WebClient webClient;


	// 비동기 호출 메서드
	public Mono<String> get(int postId) {
		// GET 요청을 보내고, 결과를 비동기적으로 반환
		return webClient.get()
				.uri("/posts/{id}", postId) // URI에 파라미터 바인딩
				.retrieve()                // HTTP 응답을 처리
				.bodyToMono(String.class); // 응답을 Mono로 반환
	}


	// 비동기 POST 호출
	public Mono<String> post(SampleRequest sampleRequest) {
		// POST 요청 생성
		return webClient.post()
				.uri("/posts")           // API 경로
				.bodyValue(sampleRequest)    // 요청 본문 설정
				.retrieve()                  // 응답 처리 시작
				.bodyToMono(String.class);   // 응답을 Mono로 반환
	}


	// 동기로 데이터 가져오기
	public String getSync(int postId) {
		return get(postId).block(); // 비동기 결과를 동기 방식으로 처리
	}


	// 동기로 데이터 가져오기
	public String postSync(SampleRequest sampleRequest) {
		return post(sampleRequest).block(); // 비동기 결과를 동기 방식으로 처리
	}
}
