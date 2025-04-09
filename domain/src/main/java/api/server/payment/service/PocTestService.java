package api.server.payment.service;

import api.server.common.exception.custom.BusinessException;
import api.server.fixedlength.helper.FixedLengthHelper;
import api.server.payment.request.PaymentRequest;
import api.server.van.request.KiccVanRequest;
import api.server.van.response.KiccVanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ddf.EscherColorRef;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PocTestService {

	private final SocketService socketService;

	public KiccVanResponse procPayment(PaymentRequest paymentRequest) {

		KiccVanRequest kiccVanRequest = KiccVanRequest.builder()
				.encryptionFlag("2")
				.changeDate("2504")
				.terminalId(paymentRequest.getStoreId())
				.checkCardNumber("")
				.serialNumber("000000000001")
				.timeout("15")
				.managerName("홍길동")
				.companyTerminalNumber(paymentRequest.getStoreId())
				.extendedTerminalNumber(paymentRequest.getStoreId())
				.reservedField("")
				.messageType(paymentRequest.getMessageType())
				.posEntryMode("1")
				.cardNumber(paymentRequest.getCardNumber())
				.installment(paymentRequest.getInstallmentPeriod())
				.currencyType("1")
				.decimalPoint("0")
				.supplyAmount(paymentRequest.getTransactionAmount())
				.serviceCharge(paymentRequest.getServiceCharge())
				.tax(paymentRequest.getTax())
				.approvalNumber("")
				.transactionDate(paymentRequest.getSentDateTime().substring(2, 8))
				.workingKeyIndex("00")
				.password("")
				.productCode("AB123")
				.idOrBusinessNumber("")
				.commerceFlag("1")
				.domain("example.com")
				.serverIp("192.168.0.1")
				.merchantBusinessNumber("")
				.cardSortCode("1")
				.merchantCompanyId("AA")
				.merchantCustomField("CUSTOMDATA000000000000000000000")
				.checkNumber("")
				.checkBankCode("")
				.checkBranchCode("")
				.bondTypeCode("")
				.checkAmount("")
				.checkIssuedDate("")
				.accountInputNumber("")
				.cvv2(paymentRequest.getCvc())
				.reserved2("")
				.digitalCertType(" ")
				.mpiModule(" ")
				.cavvReuse(paymentRequest.getCavv() != null && !paymentRequest.getCavv().isBlank() ? "Y" : "N")
				.digitalCertData("")
				.build();

		String tempFixed = FixedLengthHelper.toFixedLengthString(kiccVanRequest);
		String totalLength = String.format("%04d", tempFixed.length());
		kiccVanRequest.setTotalLength(totalLength);

		String finalFixedRequest = FixedLengthHelper.toFixedLengthString(kiccVanRequest);
		// String vanRes = socketService.sendFixedLengthRequest(finalFixedRequest);
		String vanRes = "004122951762       SOFTF3w2RGHKfAhOG30KRPARTNER           1566-3441    010-8651-7519                                           1131030022453495O250403000007417971******0297    270900000000027151해외비자            OK: 241649                                      241649      해외비자            2501711482229        전표:매입사제출";
		// 응답값 반환
        return FixedLengthHelper.fromFixedLengthString(vanRes, KiccVanResponse.class);

	}


}
