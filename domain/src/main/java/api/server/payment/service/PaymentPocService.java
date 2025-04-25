package api.server.payment.service;

import api.server.fixedlength.helper.FixedLengthHelper;
import api.server.payment.request.PaymentRequest;
import api.server.socket.SocketClient;
import api.server.van.request.KiccVanRequest;
import api.server.van.response.KiccVanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentPocService {

	private final SocketClient socketClient;

	public KiccVanResponse procPayment(PaymentRequest paymentRequest) throws Exception {

		KiccVanRequest kiccVanRequest = KiccVanRequest.builder()

				.encryptionFlag("2")
				.changeDate("2504")
				.terminalId(paymentRequest.getMerchantId())
				.checkCardNumber("")
				.serialNumber("000000000001")
				.timeout("15")
				.managerName("홍길동")
				.companyTerminalNumber(paymentRequest.getMerchantId())
				.extendedTerminalNumber(paymentRequest.getMerchantId())
				.reservedField("")
				.messageType(paymentRequest.getMessageType())
				.posEntryMode("1")
				.cardNumber(paymentRequest.getCardNumber())
				.installment(paymentRequest.getInstallmentPeriod())
				.currencyType("1")
				.decimalPoint("0")
				.supplyAmount(paymentRequest.getAmount())
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

		//String finalFixedRequest = FixedLengthHelper.toFixedLengthString(kiccVanRequest);
		//log.info("finalFixedRequest: {}", finalFixedRequest);
		//String vanRes = socketClient.sendFixedLengthRequest(finalFixedRequest);

		//통신 확인을 위해서 하드코딩 값으로 일단 연동 테스트 진행
		String rawFixedVanRequest = "0624004127721694   SOFTF3POIn4Xt6E8530KRPARTNER           1566-3441    010-8651-7519                                           1130K448558******0017=2512                0010000000000100000000000000000000000000                  AA0000000000000000                7https://www.eximbay.com                 61.78.75.98         20186358391  3DS                                                                                                                                            ***CVCDATA                    MFNAAECBDRlgAAAAABkQQCHdAAAAAA=            584ec113-cdb8-4167-9241-2d6aee14191c    0561.255.130.210                                                                                                                              ";
		log.info("rawFixedVanRequest: {}", rawFixedVanRequest);
		String vanRes = socketClient.sendFixedLengthRequest(rawFixedVanRequest);

        return FixedLengthHelper.fromFixedLengthString(vanRes, KiccVanResponse.class);

	}


}
