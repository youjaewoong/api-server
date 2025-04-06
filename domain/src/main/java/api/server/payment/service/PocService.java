package api.server.payment.service;

import api.server.common.enums.LogType;
import api.server.common.exception.custom.BusinessException;
import api.server.common.helper.AppUtil;
import api.server.payment.request.PaymentRequest;
import api.server.payment.gateway.AbstractVer;
import api.server.webclient.WebClientSync;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PocService {

	private final WebClientSync webClientSync;
	private final ObjectMapper objectMapper;
	private final String verClasses[] = {"com.eximbay.app.gateway.v100.VerWorker"};

	/**
	 * 결제 요청 데이터를 외부 서비스로 전달하고, 응답을 파싱한 뒤
	 * 표준화된 응답 객체로 포맷팅합니다.
	 *
	 * @return 외부 서비스로부터 반환된 출력 필드를 포함한 포맷팅된 응답 객체
	 * @throws BusinessException 웹 서비스 통신 중 오류가 발생하거나,
	 *                           응답 처리 과정에서 문제가 발생할 경우 예외 발생
	 */
	public void procPayment(PaymentRequest paymentRequest) throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		Map<String, Object> dataInfo = new HashMap<>();
		byte[] sendBytes = null;
		byte[] recvBytes = null;

		String type = new String(recvBytes,  8,  4);
		if (type.equals("0100") || type.equals("0101") || type.equals("0121") || type.equals("0180") || type.equals("0181") ||type.equals("0182") || type.equals("0184") ) {
			byte[] mark_recvBytes = null;
			if (type.equals("0100") || type.equals("0101") || type.equals("0121") || type.equals("0181") ||type.equals("0182") || type.equals("0184") ) {
				mark_recvBytes = AppUtil.getMarkByte(recvBytes, "******", 115, "000", 134, "**********", 138, "**********", 244, "**********", 269); // 카드번호 MARK 시작 위치
			} else {
				mark_recvBytes = AppUtil.getMarkByte(recvBytes, "******", 115, "000", 134); // 카드번호 MARK 시작 위치
			}
			log.debug(LogType.RG.getDescription(), mark_recvBytes);
		} else {
			log.debug(LogType.RG.getDescription(), recvBytes);
		}

		//////////////////////////////////////////////////////////////////////////
		// 헤더전문 분석 및 유효성 체크
		//////////////////////////////////////////////////////////////////////////
		dataInfo = parseHeader(recvBytes);

		// 요청값 포맷팅
		log.debug("endPointProperties: {}", paymentRequest);


		//////////////////////////////////////////////////////////////////////////
		// 전문버전별 클래스 로딩
		//////////////////////////////////////////////////////////////////////////
		String ver = (String)dataInfo.get("RH01");
		String className = verClasses[Integer.parseInt(ver)-1000];
		AbstractVer worker = null;

		Class cls = null;
		cls = Class.forName(className);
		worker = (AbstractVer)cls.newInstance();
		// worker.setAppWorker(this);


	}


	private Map<String, Object> parseHeader(byte[] recvBytes) {
		Map<String, Object> dataInfo = new HashMap<>();
		//헤더 파싱
		String field01 = new String(recvBytes,  0,  4);
		String field02 = new String(recvBytes,  4,  4);
		String field03 = new String(recvBytes,  8,  4);
		String field04 = new String(recvBytes, 12,  4);
		String field05 = new String(recvBytes, 16, 14);

		dataInfo.put("RH01", field01);	//전문버전
		dataInfo.put("RH02", field02);  //Msg ID
		dataInfo.put("RH03", field03);  //Message Type
		dataInfo.put("RH04", field04);  //응답코드
		dataInfo.put("RH05", field05);  //전송일시

		return dataInfo;
	}
}
