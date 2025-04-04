package api.server.payment;

import api.server.payment.request.PaymentRequest;
import api.server.payment.service.PocService;
import api.server.restapi.request.RestAPIRequest;
import api.server.restapi.response.common.RestAPIResponse;
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
@RequestMapping("poc-server")
public class PocController {

    private final PocService pocService;

    @Operation(summary = "해외신용카드 결제처리",
            description = "해외신용카드 결제처리 합니다.",
            responses = {@ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @PostMapping(value = "payment")
    ResponseEntity<RestAPIResponse> procPayment(@RequestBody PaymentRequest paymentRequest) {
        return ResponseEntity.ok(pocService.procPayment(paymentRequest));
    }
}
