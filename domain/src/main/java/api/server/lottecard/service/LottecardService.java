package api.server.lottecard.service;

import api.server.common.exception.custom.BusinessException;
import api.server.fixedlength.response.common.FixedLengthResponseFormatterFactory;
import api.server.lottecard.service.enums.LottecardErrorCode;
import api.server.restapi.request.RestAPIRequest;
import api.server.restapi.response.common.RestAPIResponse;
import api.server.restapi.response.common.RestAPIResponseFormatter;
import api.server.restapi.response.common.RestAPIResponseFormatterFactory;
import api.server.webclient.WebClientSync;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class LottecardService {

	private final WebClientSync webClientSync;

	@Qualifier("taskExecutor")
	private final ThreadPoolTaskExecutor taskExecutor;

	private final ObjectMapper objectMapper;

	public LottecardService(@Qualifier("taskExecutor") ThreadPoolTaskExecutor taskExecutor,
							WebClientSync webClientSync,
							ObjectMapper objectMapper) {

		this.taskExecutor = taskExecutor;
		this.webClientSync = webClientSync;
		this.objectMapper = objectMapper;
	}


	/**
	 * API 전문 조회
	 *
	 * @param restAPIRequest 요청 데이터
	 * @return 응답 전문 반환
	 */
	@Async("taskExecutor")
	public CompletableFuture<RestAPIResponse> post(RestAPIRequest restAPIRequest) {

		return CompletableFuture.supplyAsync(() -> {
			try {
				String result = webClientSync.post(restAPIRequest);
				Map<String, Object> resultMap = objectMapper.readValue(result, new TypeReference<>(){});

				RestAPIResponseFormatter formatter = RestAPIResponseFormatterFactory.getFormatter(restAPIRequest.getGramNo());
				return formatter.formatResponse(resultMap);

			} catch (Exception e) {
				log.error("post BusinessException: {}", e.getMessage());
				throw new BusinessException(LottecardErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()); // 래핑
			}
		}, taskExecutor).exceptionally(e -> {
			log.error("Async processing failed: {}", e.getMessage(), e);
			throw new BusinessException(LottecardErrorCode.INTERNAL_SERVER_ERROR);
		});

	}


	/**
	 * API 전문 처리
	 *
	 * @param restAPIRequest 요청 데이터
	 */
	public void voidPost(RestAPIRequest restAPIRequest) {
		try {
			 webClientSync.post(restAPIRequest);
		} catch (Exception e) {
			log.error("voidPost BusinessException: {}", e.getMessage());
			throw new BusinessException(LottecardErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()); // 래핑
		}
	}
}
