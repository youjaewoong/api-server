package api.server.gateway.infrastructure.mapper;

import api.server.payment.request.PaymentRequest;
import api.server.gateway.infrastructure.entity.MerchantServiceEntity;
import api.server.gateway.infrastructure.entity.TransactionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;


/**
 * VAN 검증 정보 매퍼 인터페이스
 */
@Mapper
public interface VerMapper {


    /**
     * 결제요청 데이터 유효성 확인 및 부가 데이터 추출
     *
     * @param merchantId 가맹점 ID
     * @param currency 통화
     * @return 가맹점 서비스 정보
     */
    MerchantServiceEntity checkRequest(
            PaymentRequest paymentRequest
    );

    /**
     * 거래 웹 정보 저장
     *
     * @param transactionWeb 거래 웹 정보
     * @return 생성된 웹 거래 번호
     */
    int reqTransactWeb(PaymentRequest paymentRequest);

    /**
     * 승인통화 체크
     *
     * @param merchantId 가맹점 ID
     * @param cardCode 카드 코드
     * @param currency 통화
     * @return 승인통화 정보
     */
    Map<String, Object> checkApprvCurrency(PaymentRequest paymentRequest
    );

    /**
     * 거래 정보 저장 준비
     *
     * @param merchantId 가맹점 ID
     * @param merchantNo 가맹점 번호
     * @param serviceCode 서비스 코드
     * @param cardCode 카드 코드
     * @param currency 통화
     * @return VAN 정보
     */
    Map<String, Object> prepareReqTranact(
            @Param("merchantId") String merchantId,
            @Param("merchantNo") String merchantNo,
            @Param("serviceCode") String serviceCode,
            @Param("cardCode") String cardCode,
            @Param("currency") String currency
    );

    /**
     * 거래 정보 저장
     *
     * @param transaction 거래 정보
     * @return 생성된 거래 번호
     */
    int reqTransact(TransactionEntity transaction);


}
