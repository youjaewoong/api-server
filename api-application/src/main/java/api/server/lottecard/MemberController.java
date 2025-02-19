package api.server.lottecard;

import api.server.common.constant.GramInfoConstant;
import api.server.lottecard.service.LottecardService;
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

import java.util.concurrent.CompletableFuture;

/**
 * 롯데카드 회원전문 API
 */
@Tag(name = "회원", description = "회원 전문을 호출 합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("api-server/member")
public class MemberController {

    private final LottecardService lottecardService;

    @Operation(summary = "회원정보 목록 정보 조회",
            description = "회원정보 목록 정보를 조회합니다.",
            responses = {@ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @PostMapping(value = "pis000062")
    ResponseEntity<CompletableFuture<RestAPIResponse>> pis000062(@RequestBody RestAPIRequest restAPIRequest) {

        restAPIRequest.setGramNo(GramInfoConstant.PIS000062); // 회원정보 전문
        restAPIRequest.setRspBizDc(GramInfoConstant.TEST); // 회원코드
        return ResponseEntity.ok(lottecardService.post(restAPIRequest));
    }
}
