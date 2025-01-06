package api.server.fixedlength.service;

import api.server.common.exception.custom.BusinessException;
import api.server.common.helper.FilePathHelper;
import api.server.common.message.MessageHelper;
import api.server.common.properties.GramProperties;
import api.server.fixedlength.cache.FixedLengthJsonCache;
import api.server.fixedlength.cache.HeaderCache;
import api.server.fixedlength.enmus.FixedLengthErrorCode;
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

    private final GramProperties gramProperties;

    private final FilePathHelper filePathHelper;

    private final MessageHelper messageHelper;

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

                // Step 1: 헤더 생성 및 캐싱
                String headerData = HeaderCache.getHeader(gramProperties.getType(), fixedLengthRequest);
                log.info("Header: {}", headerData);
                log.info("Header Length: {}", headerData.length());

                // Step 2: JSON 데이터 로드 및 캐싱
                String jsonFilePath =  filePathHelper.getFilePath(fixedLengthRequest.getGramId());
                FixedLengthJsonVO jsonModel = FixedLengthJsonCache.getJson(jsonFilePath);
                log.info("jsonModel: {}", jsonModel);

                // Step 3: 요청 바디 데이터 생성
                String bodyData = FixedLengthHelper.toFixedLengthBody(jsonModel.getInFields(), fixedLengthRequest.getInFields());
                log.info("Body: {}", bodyData);
                log.info("Body Length: {}", bodyData.length());

                // Step 4: 헤더와 바디 결합
                String fixedLengthData = headerData + bodyData;
                log.info("FixedLengthData: {}", fixedLengthData);

                // Step 5: 소켓 통신
                String fixedLengthResponse = sendFixedLengthRequest(fixedLengthData);
                log.info("FixedLengthResponse: {}", fixedLengthResponse);

                // Step 6: 고정 길이 데이터를 JSON으로 매핑
                Map<String, String> outFields = FixedLengthJsonLoaderHelper.mapFixedLengthToJson(fixedLengthResponse, jsonModel.getOutFields());

                // Step 7: 길이 계산
                int inHeaderTotal = headerData.length();
                int inBodyTotal = FixedLengthLengthCalculatorHelper.calculateTotalLength(jsonModel.getInFields());
                int outBodyTotal = FixedLengthLengthCalculatorHelper.calculateTotalLength(jsonModel.getOutFields());

                // Step 8: 응답 생성
                return FixedLengthResponse.createResponse(
                        fixedLengthRequest.getGramId(),
                        fixedLengthRequest.getServiceId(),
                        inHeaderTotal,
                        inBodyTotal,
                        outFields,
                        outBodyTotal);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw e;
            }
        });
    }

    private String sendFixedLengthRequest(String request) {
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
