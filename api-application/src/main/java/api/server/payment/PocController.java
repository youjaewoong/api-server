package api.server.payment;

import api.server.payment.request.PaymentRequest;
import api.server.payment.service.PocService;
import api.server.payment.service.PocTestService;
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

@Tag(name = "결제", description = "KICC 전문을 처리 합니다.")
@RestController
@RequiredArgsConstructor
public class PocController {

    private final PocService pocService;
    private final PocTestService pocTestService;

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
    @PostMapping(value = "payment")
    public ResponseEntity<KiccVanResponse> processPayment(@RequestBody PaymentRequest paymentRequest) {
        KiccVanResponse resonse =
                pocTestService.procPayment(paymentRequest);
        return ResponseEntity.ok(resonse);
    }
}