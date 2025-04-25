package api.server.payment.service;

import api.server.common.enums.LogType;
import api.server.common.helper.AppUtil;
import api.server.gateway.legacy.AbstractWorker;
import api.server.gateway.legacy.AppData;
import api.server.gateway.legacy.v100.VerData;
import api.server.gateway.legacy.v100.VerWorker;
import api.server.payment.request.PaymentRequest;
import api.server.webclient.WebClientSync;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLegacyService extends AbstractWorker {

	private final WebClientSync webClientSync;
	private final ObjectMapper objectMapper;
	private final String verClasses[] = {"api.server.gateway.legacy.v100.VerWorker"};
	public AppData appData;
	private OutputStream out;

	/**
	 * 결제 요청 데이터를 외부 서비스로 전달하고, 응답을 파싱한 뒤
	 * 표준화된 응답 객체로 포맷팅합니다.
	 *
	 * @return 외부 서비스로부터 반환된 출력 필드를 포함한 포맷팅된 응답 객체
	 */
	public void procPayment(PaymentRequest paymentRequest) throws Exception {

		Map<String, String> dataInfo = new HashMap<>();
		byte[] sendBytes = null;
		byte[] recvBytes = "1000KICC0100    20250331000039D34B10D069D34B10D069W0000129048525KRW00000001260000000000000000000000000000C002374695******006     2910 000 **********          KRN                                                                                   **********               **********".getBytes();

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
		//레거시 분석 2단계
		dataInfo = parseHeader(recvBytes);

		// 요청값 포맷팅
		log.debug("endPointProperties: {}", paymentRequest);


		//////////////////////////////////////////////////////////////////////////
		// 전문버전별 클래스 로딩
		//////////////////////////////////////////////////////////////////////////
		String ver = (String)dataInfo.get("RH01");
		String className = verClasses[Integer.parseInt(ver)-1000];
		//AbstractVer worker;

		Class cls;
		cls = Class.forName(className);
		// worker = (AbstractVer)cls.newInstance();
		//worker.setAppWorker(new VerWorker());
		VerWorker worker = new VerWorker();
		appData = new VerData();

		//////////////////////////////////////////////////////////////////////////
		// 전문버전별 전문분석
		//////////////////////////////////////////////////////////////////////////
		//레거시 분석 3단계
		dataInfo = appData.parseData(recvBytes);


		//////////////////////////////////////////////////////////////////////////
		// 전문버전별 처리
		//////////////////////////////////////////////////////////////////////////
		//레거시 분석 4단계
		worker.procTransaction(dataInfo);


		//////////////////////////////////////////////////////////////////////////
		// 응답데이터 구성
		//////////////////////////////////////////////////////////////////////////
		sendBytes = appData.makeData();


		//////////////////////////////////////////////////////////////////////////
		// 응답데이터 전송
		//////////////////////////////////////////////////////////////////////////
		try{
			//write(out, sendBytes);
			if (type.equals("0100") || type.equals("0101") || type.equals("0121") || type.equals("0180") || type.equals("0181") ||type.equals("0182") || type.equals("0184") ) {
				byte[] mark_sendBytes = null;
				mark_sendBytes = AppUtil.getMarkByte(sendBytes, "******", 119, "**********", 137); // 카드번호 MARK 시작 위치
				log.info(LogType.RG.getDescription(), mark_sendBytes);

			} else {
				log.info(LogType.SG.getDescription(), sendBytes);
			}
			//accessLog(SG, sendBytes);
			//accessLog(SG);
			sendData(out, sendBytes);
		}catch(Exception e){
			throw e;
		}
	}


	private Map<String, String> parseHeader(byte[] recvBytes) {
		Map<String, String> dataInfo = new HashMap<>();
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

	@Override
	public void initWorker() {

	}

	@Override
	public boolean isDisconnect() {
		return false;
	}

	@Override
	public void runTransaction(Socket s, InputStream in, OutputStream out) throws Exception {

	}
}
