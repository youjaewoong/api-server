package api.server.fixedlength.service;

import api.server.common.exception.custom.BusinessException;
import api.server.common.properties.GramProperties;
import api.server.fixedlength.cache.FixedLengthJsonCache;
import api.server.fixedlength.cache.HeaderCache;
import api.server.fixedlength.helper.FixedLengthHelper;
import api.server.fixedlength.helper.FixedLengthJsonLoaderHelper;
import api.server.fixedlength.helper.FixedLengthLengthCalculatorHelper;
import api.server.fixedlength.request.FixedLengthRequest;
import api.server.fixedlength.response.common.FixedLengthResponse;
import api.server.fixedlength.response.common.ResponseFormatter;
import api.server.fixedlength.response.common.ResponseFormatterFactory;
import api.server.fixedlength.socket.FixedLengthTestSocketProcessor;
import api.server.fixedlength.vo.FixedLengthJsonVO;
import api.server.gramstorage.helpler.GramFilePathHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;


/**
 * <pre>
 * 온라인 인터페이스 유형에 따라 메소드가 구성되어 있습니다.
 * </pre>
 */
@Service
@Slf4j
public class FixedLengthService {

    private final GramProperties gramProperties;

    private final GramFilePathHelper gramFilePathHelper;

    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor taskExecutor;


    public FixedLengthService(@Qualifier("taskExecutor") ThreadPoolTaskExecutor taskExecutor,
                              GramProperties gramProperties,
                              GramFilePathHelper gramFilePathHelper) {

        this.taskExecutor = taskExecutor;
        this.gramProperties = gramProperties;
        this.gramFilePathHelper = gramFilePathHelper;
    }

    /**
     * Sync (동기) 요청 처리 및 응답 생성.
     *
     * <pre>
     *  - 소스 시스템의 요청 건에 대해 대상 시스템의 처리 결과를 즉시 받아야 하는 유형.
     *  - 주로 거래 특성상 응답을 바로 처리해야 하거나, 거래량이 많지 않고 타겟 시스템의 응답 속도가 빠를 경우 사용.
     *  - 소스 시스템에서 요청/응답 처리를 하나의 서비스에서 구현.
     *  - 사용 예:
     *      a. 신용정보 조회 거래 (예: 한신평, 은행연합회 등).
     *      b. 일반적으로 타임아웃 시간 30초로 설정.
     * </pre>
     *
     * @param fixedLengthRequest 요청 데이터
     * @return 응답 데이터
     */
    public FixedLengthResponse findFixedLengthSync(FixedLengthRequest fixedLengthRequest) {
        return this.getFixedLengthResponse(fixedLengthRequest);
    }


    /**
     * Async (비동기) 요청 처리 및 응답 생성.
     *
     * <pre>
     *  - 거래량이 많거나 타겟 시스템의 처리가 오래 걸릴 경우 사용.
     *  - 소스 시스템이 응답을 기다리지 않고 다른 업무를 처리할 수 있음.
     *  - 요청/응답 처리를 분리하여 구현.
     *  - 사용 예:
     *    a. 즉시 출금, 송금 환불 등.
     *    b. 거래 특성상 매우 Critical한 사항(예: 입출금 관련).
     * </pre>
     *
     * @param fixedLengthRequest 요청 데이터
     */
    @Async("taskExecutor")
    public CompletableFuture<FixedLengthResponse> findFixedLengthAsync(FixedLengthRequest fixedLengthRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.getFixedLengthResponse(fixedLengthRequest); // 동기 작업
            } catch (BusinessException ex) {
                log.error("BusinessException: {}", ex.getMessage(), ex);
                throw new CompletionException(ex); // 래핑
            }
        }, taskExecutor).exceptionally(ex -> {
            log.error("Async processing failed: {}", ex.getMessage(), ex);
            throw new CompletionException(ex);
        });
    }



    /**
     * Notify (단방향) 요청 처리 및 Void.
     *
     * <pre>
     *  - 대상 시스템의 응답이 필요 없는 단순 전달 인터페이스 유형.
     *  - 단순 데이터 전달이 목적이며, 주로 일별 대사 배치가 존재.
     *  - 사용 예 : 에버리치 체크카드 B/L 등재.
     * </pre>
     *
     * @param fixedLengthRequest 요청 데이터
     */
    public void procFixedLengthNotify(FixedLengthRequest fixedLengthRequest) {
        this.getFixedLengthResponse(fixedLengthRequest);
    }


    /**
     * Deferred (지연 처리 / 순서 보장) 요청 처리 및 Void.
     *
     * <pre>
     *  - 전문의 순서가 보장되어야 하고, 순서가 맞지 않을 경우 결번 요청 프로세스가 필요한 경우 사용.
     *  - 소스 시스템에서 응답을 기다리지 않음.
     *  - 사용 예 : a. 입금, 출금, 고객 정보 전달 등 승인계 연동 거래.
     * </pre>
     *
     * @param fixedLengthRequest 요청 데이터
     */
    public void procFixedLengthDeferred(FixedLengthRequest fixedLengthRequest) {
        this.getFixedLengthResponse(fixedLengthRequest);
    }


    /**
     * 공통 비즈니스 로직 처리
     *
     * @param fixedLengthRequest 요청 데이터
     * @return 공통 응답 객체
     */
    private FixedLengthResponse getFixedLengthResponse(FixedLengthRequest fixedLengthRequest) {
        // Step 1: 헤더 생성 및 캐싱
        String headerData = HeaderCache.getHeader(gramProperties.getType(), fixedLengthRequest);
        log.info("Header: {}", headerData);
        log.info("Header Length: {}", headerData.length());

        // Step 2: JSON 데이터 로드 및 캐싱
        String jsonFilePath =  gramFilePathHelper.getFilePath(fixedLengthRequest.getGramId());
        FixedLengthJsonVO jsonModel = FixedLengthJsonCache.getJson(jsonFilePath);
        log.info("jsonModel: {}", jsonModel);

        // Step 3: 요청 바디 데이터 생성
        String bodyData = FixedLengthHelper.toFixedLengthBody(jsonModel.getInFields(), fixedLengthRequest.getInFields());
        log.info("Body: {}", bodyData);
        log.info("Body Length: {}", bodyData.length());

        // Step 4: 헤더와 바디 결합
        String fixedLengthData = headerData + bodyData;
        log.info("FixedLengthData: {}", fixedLengthData);

        // Step 5: 소켓 통신 템플릿 메소드 패턴 처리
        FixedLengthTestSocketProcessor processor = new FixedLengthTestSocketProcessor();
        String fixedLengthResponse = processor.process(fixedLengthData);
        log.info("FixedLengthResponse: {}", fixedLengthResponse);

        // Step 6: 고정 길이 데이터를 JSON 으로 매핑
        Map<String, Object> outFields =
                FixedLengthJsonLoaderHelper.mapFixedLengthToJson(fixedLengthResponse, jsonModel.getOutFields());

        // Step 7: 길이 계산
        int inHeaderTotal = headerData.length();
        int inBodyTotal = FixedLengthLengthCalculatorHelper.calculateTotalLength(jsonModel.getInFields());
        int outBodyTotal = FixedLengthLengthCalculatorHelper.calculateTotalLength(jsonModel.getOutFields());

        // Step 8: 응답 생성 팩토리
        ResponseFormatter formatter = ResponseFormatterFactory.getFormatter(fixedLengthRequest.getGramId());
        return formatter.formatResponse(
                 fixedLengthRequest.getGramId()
                ,fixedLengthRequest.getServiceId()
                ,inHeaderTotal
                ,inBodyTotal
                ,outFields
                ,outBodyTotal);
    }

}
