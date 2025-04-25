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
public class PaymentTestController {

    private final PaymentLegacyService paymentLegacyService;
    private final PaymentPocService paymentPocService;


    @Operation(
            summary = "해외신용카드 PoC 결제처리",
            description = "해외신용카드 PoC 결제처리를 수행합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "결제 성공",
                            content = @Content(schema = @Schema(implementation = PaymentRequest.class))
                    )
            }
    )
    @PostMapping(value = "poc")
    public ResponseEntity<KiccVanResponse> pocApprove(@RequestBody PaymentRequest paymentRequest) throws Exception {
        KiccVanResponse response =
                paymentPocService.procPayment(paymentRequest);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "해외신용카드 레거시 테스트",
            description = "해외신용카드 레거시 테스트를 수행합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "결제 성공",
                            content = @Content(schema = @Schema(implementation = PaymentRequest.class))
                    )
            }
    )
    @PostMapping(value = "legacy")
    public ResponseEntity<String> legacyApprove(@RequestBody PaymentRequest paymentRequest) throws Exception {
        //레거시 분석 1단계
        paymentLegacyService.procPayment(paymentRequest);
        return ResponseEntity.ok("OK");
    }


}