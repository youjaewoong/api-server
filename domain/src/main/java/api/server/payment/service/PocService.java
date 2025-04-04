package api.server.payment.service;

import api.server.common.exception.custom.BusinessException;
import api.server.payment.request.PaymentRequest;
import api.server.restapi.request.RestAPIRequest;
import api.server.restapi.response.common.RestAPIResponse;
import api.server.webclient.WebClientSync;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PocService {

	private final WebClientSync webClientSync;
	private final ObjectMapper objectMapper;


	/**
	 * 결제 요청 데이터를 외부 서비스로 전달하고, 응답을 파싱한 뒤
	 * 표준화된 응답 객체로 포맷팅합니다.
	 *
	 * @param restAPIRequest 결제 처리 작업에 필요한 입력 필드를 포함하는 요청 객체
	 * @return 외부 서비스로부터 반환된 출력 필드를 포함한 포맷팅된 응답 객체
	 * @throws BusinessException 웹 서비스 통신 중 오류가 발생하거나,
	 *                           응답 처리 과정에서 문제가 발생할 경우 예외 발생
	 */
	public RestAPIResponse procPayment(PaymentRequest paymentRequest) {

		// 요청값 포맷팅
		log.debug("endPointProperties: {}", paymentRequest);
		return null;
	}
}
