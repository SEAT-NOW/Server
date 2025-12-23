package depth.main.seatnow.domain.auth.controller;

import depth.main.seatnow.domain.auth.dto.request.VerifyBusinessNumberRequest;
import depth.main.seatnow.domain.auth.service.BusinessVerificationService;
import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.global.exception.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Verification", description = "각종 인증(이메일, SMS, 사업자) API")
@RestController
@RequestMapping("/api/v1/auth/verify")
@RequiredArgsConstructor
public class VerificationController {
    private final BusinessVerificationService ownerBusinessService;
    @Operation(
            summary = "사업자 등록번호 유효성 인증",
            description = "국세청 데이터를 기반으로 사업자 번호의 유효 여부를 확인합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "인증 성공 (계속사업자)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = "{\"success\": true, \"data\": true, \"message\": \"사업자 등록번호가 유효하게 확인되었습니다.\"}"
                            ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (사용자 입력 문제)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "미등록 번호",
                                            summary = "INVALID_BUSINESS_NUMBER (4001)",
                                            value = "{\"code\": \"4001\", \"message\": \"유효하지 않은 사업자번호입니다.\", \"detail\": \"국세청에 등록되지 않은 사업자 번호입니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "휴/폐업 상태",
                                            summary = "INVALID_BUSINESS_NUMBER (4001)",
                                            value = "{\"code\": \"4001\", \"message\": \"유효하지 않은 사업자번호입니다.\", \"detail\": \"현재 휴업자 상태입니다. 계속사업자만 등록 가능합니다.\"}"
                                    )
                            })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류 (외부 API 통신 장애)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "국세청 서버 응답 없음",
                                    summary = "EXTERNAL_API_ERROR (5001)",
                                    value = "{\"code\": \"5001\", \"message\": \"외부 시스템과의 통신 중 오류가 발생했습니다.\", \"detail\": \"국세청 API 서버 응답이 없습니다.\"}"
                            ))
            )
    })
    @PostMapping("/business")
    public ApiResponse<Boolean> verify(@RequestBody VerifyBusinessNumberRequest request) {
        boolean result = ownerBusinessService.verifyBusinessNumber(request.getBusinessNumber());
        return ApiResponse.ok(result, "사업자 등록번호가 유효하게 확인되었습니다.");
    }
}
