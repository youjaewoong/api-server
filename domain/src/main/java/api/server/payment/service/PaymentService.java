package api.server.payment.service;

import api.server.payment.constant.PaymentConstant;
import api.server.payment.errors.CardErrorCode;
import api.server.payment.request.PaymentRequest;
import api.server.gateway.infrastructure.mapper.VerMapper;
import api.server.exception.custom.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final VerMapper verMapper;

    /**
     * 결제 요청 데이터를 외부 서비스로 전달하고, 응답을 파싱한 뒤
     * 표준화된 응답 객체로 포맷팅합니다.
     *
     * @return 외부 서비스로부터 반환된 출력 필드를 포함한 포맷팅된 응답 객체
     * @throws BusinessException 웹 서비스 통신 중 오류가 발생하거나,
     *                           응답 처리 과정에서 문제가 발생할 경우 예외 발생
     */
    public void procPayment(PaymentRequest paymentRequest) throws Exception {

        // TODO 전문별 구성

        // 전문버전별 전문분석

        // 전문버전별 처리
        this.procF0100(paymentRequest);

        // 응답데이터 구성

        // TODO 기존 소캣 -> REST API 응답

    }

    private void procF0100(PaymentRequest paymentRequest) {

        int format = Integer.parseInt(paymentRequest.getMessageType());
        if (format == PaymentConstant.F0101 || format == PaymentConstant.F0121) {
            // 필수 데이터 확인
            String cardType = paymentRequest.getCardType();
            if (StringUtils.isEmpty(paymentRequest.getCardType())) {
                throw new BusinessException(CardErrorCode.INVALID_CURRENCY);
            } else if (StringUtils.isEmpty(paymentRequest.getMerchantRef()) || Double.parseDouble(paymentRequest.getMerchantRef()) <= 0) {
                throw new BusinessException(CardErrorCode.INVALID_AMOUNT);
            } else if (StringUtils.isEmpty(paymentRequest.getMerchantRef())) {
                throw new BusinessException(CardErrorCode.INVALID_MERCHANT_REF);
            } else if (StringUtils.isEmpty(paymentRequest.getBuyerName())) {
                throw new BusinessException(CardErrorCode.INVALID_BUYER);
            } else if (cardType.equals("") || (!cardType.equals("C000") && !cardType.equals("C001") && !cardType.equals("C002") && !cardType.equals("C003") && !cardType.equals("C026"))) {
                throw new BusinessException(CardErrorCode.INVALID_CARD_TYPE);
            } else if (paymentRequest.getCardNumber().length() < 12) {
                throw new BusinessException(CardErrorCode.INVALID_CARD_NUMBER);
            } else if (paymentRequest.getExpiryDate().length() != 4) {
                throw new BusinessException(CardErrorCode.INVALID_EXPIRY_DATE);
            } else if (StringUtils.isEmpty(paymentRequest.getMerchantId())) {
                throw new BusinessException(CardErrorCode.INVALID_MERCHANT_ID);
            }

            //허용 가맹점 IP 체크
            log.info("[VerWorker::procF0100]허용 가맹점 IP 체크 완료");

            // 승인금액
            String currency = paymentRequest.getCurrency().trim();
            BigDecimal amount = new BigDecimal(paymentRequest.getAmount());
            if (!currency.equals("KRW") && !currency.equals("JPY") && !currency.equals("THB")) {
                amount = amount.divide(BigDecimal.valueOf(100));
            }
            // dataInfo.put("VD_AMT", amount.toString());
            log.info("[VerWorker::procF0100]승인금액 : {}", amount);

            // 결제요청 데이터 유효성 확인 및 부가 데이터 추출
            verMapper.checkRequest(paymentRequest);
            log.info("[VerWorker::procF0100]결제요청 데이터 유효성 확인 및 부가 데이터 추출 완료");

            // 요청 정보 저장(transact_web)
            verMapper.reqTransactWeb(paymentRequest);
            log.info("[VerWorker::procF0100]요청 정보 저장(transact_web)");

            // checkApprvCurrency
            Map<String, Object> VD_ISSUEDKR = verMapper.checkApprvCurrency(paymentRequest);
            log.info("[VerWorker::procF0100]checkApprvCurrency");
            log.info("[VerWorker::procF0100]한국발급카드 : {}", VD_ISSUEDKR);

            // 한국발급카드 거절
            if (VD_ISSUEDKR.isEmpty()) {
                throw new BusinessException(CardErrorCode.CARD_ISSUED_IN_KOREA);
            }

            /*
            log.info("[VerWorker::procF0100]최소금액 : {}", dataInfo.get("VD_MINAMOUNT"));

            // 결제수단별 최소금액 확인
            if (AppUtil.checkNullDouble(dataInfo.get("VD_MINAMOUNT")) > amount.doubleValue()) {
                throw new BusinessException(CardErrorCode.BELOW_MINIMUM_AMOUNT);
            }

            log.info("[VerWorker::procF0100]요청/승인통화 : {}/{}", currency, dataInfo.get("VD_APPRVCUR"));

            // 요청통화와 승인통화는 반드시 같아야 함
            if (!AppUtil.checkNull(dataInfo.get("VD_APPRVCUR")).equals(currency)) {
                throw new BusinessException(CardErrorCode.CURRENCY_MISMATCH);
            }
            //요청 정보 저장(transact)
            if (format == AppData.F0121) dataInfo.put("VD_TRANSTYPE", "T003");
            else dataInfo.put("VD_TRANSTYPE", "T000");

            dataInfo.put("VD_TRADETYPE", "T102");

            verMapper.prepareReqTranact(dataInfo);
            verMapper.reqTransact(dataInfo);
            System.out.println("[VerWorker::procF0100]요청 정보 저장(transact) 완료");

            //일반 전문 format으로 변경
            if (format == AppData.F0101) {
                dataInfo.put("RH00", String.valueOf(AppData.F0100));
                dataInfo.put("RH03", "0100");
            }
            else if (format == AppData.F0121) {
                dataInfo.put("RH00", String.valueOf(AppData.F0120));
                dataInfo.put("RH03", "0120");
            }
            */
        }
        /*
        if (format == AppData.F0102) {
            if (AppUtil.checkNull(dataInfo.get("RB72")).equals("04")) {// 삼성UPOP의 경우
                if (AppUtil.checkNull(dataInfo.get("RB73")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0100]", "W130", "invalid traceno", "invalid traceno");
            }
        }
         */
    }

}
