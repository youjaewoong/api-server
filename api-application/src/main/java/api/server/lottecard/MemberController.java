package api.server.lottecard;

import api.server.restapi.WebClientSyncService;
import api.server.restapi.request.RestAPIRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 롯데카드 회원전문 API layer
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("api-server/member")
public class MemberController {

    private LottecardService lottecardService;

    @Operation(summary = "회원정보(PIS000062) 목록 정보 조회",
            description = "회원정보(PIS000062) 목록 정보를 조회합니다.",
            responses = {@ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @PostMapping(value = "PIS000062")
    ResponseEntity<String> findMember(@RequestBody RestAPIRequest restAPIRequest) {

        return ResponseEntity.ok(lottecardService.post(restAPIRequest));
    }
}
