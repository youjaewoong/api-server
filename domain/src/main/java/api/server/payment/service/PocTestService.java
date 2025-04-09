package api.server.payment.service;

import api.server.common.exception.custom.BusinessException;
import api.server.fixedlength.helper.FixedLengthHelper;
import api.server.payment.request.PaymentRequest;
import api.server.van.request.KiccVanRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PocTestService {

	private final SocketService socketService;

	public String procPayment(PaymentRequest paymentRequest) {

		// paymentRequest R1, R2, R3

		// 요청 paymentRequest 데이터 Van 요청 데이터로 구성
		KiccVanRequest kiccVanRequest =
				KiccVanRequest
						.builder()
						.totalLength("") // 전체길이 필수
						.cavvReuse("")
						.build();

		String vanReq = FixedLengthHelper.toFixedLengthString(kiccVanRequest);
		kiccVanRequest.setTotalLength(vanReq);
		String fixedLengtReq = FixedLengthHelper.toFixedLengthString(kiccVanRequest);

		socketService.sendFixedLengthRequest(fixedLengtReq);

		// TODO 추가 응답값 json 파싱
		return socketService.sendFixedLengthRequest(fixedLengtReq);

	}


}
