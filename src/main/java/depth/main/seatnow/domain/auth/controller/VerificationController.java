package depth.main.seatnow.domain.auth.controller;

import depth.main.seatnow.domain.auth.dto.request.EmailSendRequest;
import depth.main.seatnow.domain.auth.dto.request.EmailVerifyRequest;
import depth.main.seatnow.domain.auth.dto.request.VerifyBusinessNumberRequest;
import depth.main.seatnow.domain.auth.service.BusinessVerificationService;
import depth.main.seatnow.domain.auth.service.EmailVerificationService;
import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.global.exception.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Verification", description = "각종 인증(이메일, SMS, 사업자) API")
@RestController
@RequestMapping("/api/v1/auth/verify")
@RequiredArgsConstructor
public class VerificationController {
    private final BusinessVerificationService ownerBusinessService;
    private final EmailVerificationService emailVerificationService;
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
        ownerBusinessService.verifyBusinessNumber(request.getBusinessNumber());
        return ApiResponse.ok(true, "사업자 등록번호가 유효하게 확인되었습니다.");
    }

    @Operation(summary = "이메일 인증 코드 발송", description = "입력한 이메일로 6자리 인증 번호를 전송합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "코드 발송 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"data\": true, \"message\": \"인증 코드가 이메일로 발송되었습니다.\"}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (형식 오류 또는 중복 이메일)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "잘못된 이메일 형식",
                                            summary = "INVALID_REQUEST",
                                            value = "{\"code\": \"4000\", \"message\": \"유효한 이메일 형식이 아닙니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "이미 가입된 이메일",
                                            summary = "CONFLICT",
                                            value = "{\"code\": \"4090\", \"message\": \"이미 존재하는 리소스입니다.\", \"detail\": \"해당 이메일로 가입된 계정이 이미 존재합니다.\"}"
                                    )
                            })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "메일 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "메일 발송 실패",
                                    summary = "EXTERNAL_API_ERROR",
                                    value = "{\"code\": \"5001\", \"message\": \"외부 시스템과의 통신 중 오류가 발생했습니다.\", \"detail\": \"메일 발송 서버와의 연결에 실패했습니다.\"}"
                            ))
            )
    })
    @PostMapping("/email/send")
    public ApiResponse<Boolean> sendVerificationCode(@Valid @RequestBody EmailSendRequest request) {
        emailVerificationService.sendVerificationCode(request.getEmail());
        return ApiResponse.ok(true,"인증 코드가 이메일로 발송되었습니다.");
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "사용자가 입력한 코드의 유효성을 검증합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "인증 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"data\": true, \"message\": \"인증에 성공하였습니다.\"}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "인증 실패 (번호 불일치 또는 시간 초과)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "인증번호 불일치",
                                            summary = "INVALID_VERIFICATION_CODE",
                                            value = "{\"code\": \"4002\", \"message\": \"인증 번호가 일치하지 않습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "인증 시간 만료",
                                            summary = "EXPIRED_VERIFICATION_CODE",
                                            value = "{\"code\": \"4003\", \"message\": \"인증 시간이 만료되었습니다. 다시 시도해주세요.\"}"
                                    )
                            })
            )
    })
    @PostMapping("/email/confirm")
    public ApiResponse<Boolean> verifyCode(@RequestBody EmailVerifyRequest request) {
        emailVerificationService.verifyCode(request.getEmail(), request.getCode());
        return ApiResponse.ok(true, "인증에 성공하였습니다.");
    }
}
