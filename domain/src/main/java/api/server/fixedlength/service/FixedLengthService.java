package api.server.fixedlength.service;

import api.server.common.exception.custom.BusinessException;
import api.server.fixedlength.enmus.FixedLengthErrorCode;
import api.server.fixedlength.request.FixedLengthRequest;
import api.server.fixedlength.util.FixedLengthUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FixedLengthService {

	@Value("${socket.server.host:localhost}")
	private String socketHost;

	@Value("${socket.server.port:9090}")
	private int socketPort;


	private static final Map<String, Integer> FIELD_LENGTHS = new LinkedHashMap<>() {{
		put("userId", 10);
		put("cardNumber", 20);
	}};


	private static final Map<String, Integer> RESPONSE_FIELD_LENGTHS = new LinkedHashMap<>() {{
		put("userName", 10);
		put("email", 20);
		put("cardType", 10);
	}};


    public Map<String, String> findFixedLengthData(FixedLengthRequest fixedLengthRequest) throws IOException {

		 // 요청 데이터를 고정 길이 전문으로 변환
		 String strFixedLengthRequest = FixedLengthUtil.toFixedLength(fixedLengthRequest.getInData(), FIELD_LENGTHS);

		 // 소켓 통신
		 String fixedLengthResponse = sendFixedLengthRequest(strFixedLengthRequest);

        // 전문 응답 수신
		return FixedLengthUtil.fromFixedLength(fixedLengthResponse, RESPONSE_FIELD_LENGTHS);
	}

	private String sendFixedLengthRequest(String request) throws IOException {
		try (Socket socket = new Socket(socketHost, socketPort);
			 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			// 전문 전송
			out.println(request);
			// 전문 응답 수신
			return in.readLine();
		} catch (Exception e) {
			throw new BusinessException(FixedLengthErrorCode.DATA_NOT_FOUND);
		}
	}

	public String uploadFixedLengthCommonHeader(MultipartFile file) {

		return null;
	}

	public Map<String, String> findFixedLengthCommonHeader() {

		return null;
	}
}
