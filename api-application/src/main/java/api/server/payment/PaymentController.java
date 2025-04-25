package api.server.payment;

import api.server.payment.request.PaymentRequest;
import api.server.payment.service.PaymentLegacyService;
import api.server.payment.service.PaymentPocService;
import api.server.payment.service.PaymentService;
import api.server.van.response.KiccVanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "결제", description = "KICC 전문을 처리 합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    // 승인 요청 타입 상수 정의
    private static final List<String> APPROVE_TYPES = Arrays.asList(
            "0100", // 신용카드 승인
            "0120", // 신용카드 인증(수동매입)
            "0101", // 신용카드 승인(PCINS)
            "0121", // 신용카드 인증(PCINS)
            "0102", // BC UPOT 신용카드 승인(자체 저장)
            "0122", // BC UPOT 신용카드 승인(자체 저장 수동매입)
            "0400", // 일반 환급요청
            "9999"  // Edgar 부하테스트 (2023.03.15)
    );

    @Operation(
            summary = "해외신용카드 결제처리",
            description = "해외신용카드 결제처리를 수행합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "결제 성공",
                            content = @Content(schema = @Schema(implementation = PaymentRequest.class))
                    )
            }
    )
    @PostMapping(value = "approve")
    public ResponseEntity<String> saveApprove(@RequestBody PaymentRequest paymentRequest) throws Exception {

        // 요청된 결제 타입이 유효한지 검증
        if (!APPROVE_TYPES.contains(paymentRequest.getMessageType())) {
            throw new IllegalArgumentException("유효하지 않은 결제 타입입니다: " + paymentRequest.getMessageType());
        }
        paymentService.procPayment(paymentRequest);
        return ResponseEntity.ok("OK");
    }


}