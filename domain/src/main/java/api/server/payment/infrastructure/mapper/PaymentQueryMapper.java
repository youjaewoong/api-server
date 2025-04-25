package api.server.payment.infrastructure.mapper;

import api.server.payment.infrastructure.entity.TransactionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface PaymentQueryMapper {

    // 카드BIN 확인
    int checkCardBin(@Param("bin") String bin);

    // 승인 통화 정보 조회
    Map<String, Object> getApprvCurrencyInfo(@Param("merchantId") String merchantId,
                                             @Param("cardCode") String cardCode,
                                             @Param("reqCurrency") String reqCurrency);

    // 환율 정보 조회
    String getExchangeNo(@Param("amt") double amt,
                         @Param("fromCurrency") String fromCurrency,
                         @Param("toCurrency") String toCurrency);


    // 조회 메소드
    Map<String, Object> selectRiskInfo(@Param("merchantId") String merchantId);
    TransactionEntity selectTransact(@Param("transId") String transId);
    TransactionEntity selectOrgTransact(@Param("orgTransId") String orgTransId);
    TransactionEntity selectOrgPartTransact(@Param("orgTransId") String orgTransId);

    // 삽입 메소드
    int insertTransact(TransactionEntity dto);

    // 갱신 메소드
    int updateTransact(TransactionEntity dto);
    int updateOrgTransact(TransactionEntity dto);
    int updateEDIRefund(@Param("transId") String transId);
    int updateEDICancel(@Param("transId") String transId);
    int updateEDIPartCancel(@Param("transId") String transId);

    // 인증 체크 메소드
    int checkAuthTransact(@Param("transId") String transId, @Param("authCode") String authCode);


}
