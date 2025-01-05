package api.server.fixedlength.service;

import api.server.common.exception.custom.BusinessException;
import api.server.common.property.GramProperty;
import api.server.fixedlength.enmus.FixedLengthErrorCode;
import api.server.fixedlength.header.Header;
import api.server.fixedlength.header.HeaderFactory;
import api.server.fixedlength.helper.FixedLengthHelper;
import api.server.fixedlength.helper.FixedLengthJsonLoaderHelper;
import api.server.fixedlength.helper.FixedLengthLengthCalculatorHelper;
import api.server.fixedlength.request.FixedLengthRequest;
import api.server.fixedlength.response.FixedLengthResponse;
import api.server.fixedlength.vo.FixedLengthJsonVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FixedLengthService {

	private final GramProperty gramProperty;

	@Value("${socket.server.host:localhost}")
	private String socketHost;

	@Value("${socket.server.port:9090}")
	private int socketPort;


	/**
	 * FixedLength 요청 처리 및 응답 생성.
	 *
	 * @param fixedLengthRequest 요청 데이터
	 * @return 응답 데이터
	 * @throws IOException 파일 처리 오류
	 */
    public CompletableFuture<FixedLengthResponse> findFixedLengthData(FixedLengthRequest fixedLengthRequest) throws IOException {
		return CompletableFuture.supplyAsync(() -> {
			try {
				// 동적 헤더 생성
				Header headerBuilder = HeaderFactory.getHeaderBuilder(gramProperty.getType());
				String headerData = headerBuilder.buildHeader(fixedLengthRequest);
				log.info("Header: {}", headerData);
				log.info("Header Length: {}", headerData.length());

				// JSON 파일 경로 동적 결정
				String jsonFilePath = getJsonFilePath(fixedLengthRequest);

				// JSON 데이터 로드 BODY
				FixedLengthJsonVO jsonModel = FixedLengthJsonLoaderHelper.loadJson(jsonFilePath, FixedLengthJsonVO.class);
				log.info("jsonModel: {}", jsonModel);

				// 요청 바디 데이터 생성
				String bodyData = FixedLengthHelper.toFixedLengthBody(jsonModel.getInFields(), fixedLengthRequest.getInFields());
				log.info("Body: {}", bodyData);
				log.info("Body Length: {}", bodyData.length());

				// 소켓 데이터 (샘플 데이터: 고정 길이)
				String fixedLengthData = headerData + bodyData;
				log.info("FixedLengthData: {}", fixedLengthData);

				// 소켓 통신
				String fixedLengthResponse = sendFixedLengthRequest(fixedLengthData);
				log.info("FixedLengthResponse: {}", fixedLengthResponse);

				// 고정 길이 데이터를 JSON으로 매핑
				Map<String, String> outFields = FixedLengthJsonLoaderHelper.mapFixedLengthToJson(fixedLengthResponse, jsonModel.getOutFields());

				// 길이 계산
				int inHeaderTotal = headerData.length();
				int inBodyTotal = FixedLengthLengthCalculatorHelper.calculateTotalLength(jsonModel.getInFields());
				int outBodyTotal = FixedLengthLengthCalculatorHelper.calculateTotalLength(jsonModel.getOutFields());

				// 응답 생성
				return FixedLengthResponse.createResponse(
						fixedLengthRequest.getGramId(),
						fixedLengthRequest.getServiceId(),
						inHeaderTotal,
						inBodyTotal,
						outFields,
						outBodyTotal);
			} catch (Exception e) {
				throw new RuntimeException("Error processing fixed length request", e);
			}
		});
	}

	private String getJsonFilePath(FixedLengthRequest fixedLengthRequest) {
		return "resources/gram/" + gramProperty.getType() + "/" + fixedLengthRequest.getGramId() + ".json";
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


}
