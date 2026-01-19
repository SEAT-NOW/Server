package depth.main.seatnow.domain.auth.controller;

import depth.main.seatnow.domain.auth.dto.request.EmailSendRequest;
import depth.main.seatnow.domain.auth.dto.request.OwnerLoginRequest;
import depth.main.seatnow.domain.auth.dto.request.SmsSendRequest;
import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.domain.auth.dto.response.AuthResponseDto;
import depth.main.seatnow.domain.auth.service.AuthService;
import depth.main.seatnow.global.exception.error.ErrorResponse;
import depth.main.seatnow.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@Tag(name = "인증/로그인", description = "카카오 로그인 및 사장님 로그인 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "카카오 로그인",
            description = "카카오 인가 코드를 받아 액세스 토큰과 리프레시 토큰을 발급합니다."
    )
    @GetMapping("/login/kakao")
    public ApiResponse<AuthResponseDto.TokenDto> kakaoLogin(
            @Parameter(description = "카카오에서 발급받은 인가 코드")
            @RequestParam("kakaoAccessToken") String kakaoAccessToken,
            HttpServletResponse response
    ) {
        //서비스 호출 (토큰만 받아옴)
        AuthResponseDto.TokenDto tokenDto = authService.kakaoLogin(kakaoAccessToken);

        //헤더에 jwt 토큰 세팅
        response.setHeader("Authorization", "Bearer " + tokenDto.getAccessToken());

        return ApiResponse.ok(tokenDto);
    }

    @Operation(
            summary = "토큰 재발급",
            description = "리프레시 토큰을 이용해 만료된 액세스 토큰을 갱신합니다."
    )
    @PostMapping("/reissue")
    public ApiResponse<AuthResponseDto.TokenDto> reissue(
            @Parameter(description = "유효한 리프레시 토큰", required = true)
            @RequestHeader("RefreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        AuthResponseDto.TokenDto tokenDto = authService.reissue(refreshToken);

        response.setHeader("Authorization", "Bearer " + tokenDto.getAccessToken());

        return ApiResponse.ok(tokenDto);
    }

    @Operation(
            summary = "로그아웃 [인증 필요]",
            description = "현재 로그인된 유저의 Refresh Token을 삭제하여 로그아웃 처리",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        authService.logout(customUserDetails.getUserId());
        return ApiResponse.ok(null);
    }

    @Operation(
            summary = "사장님 로그인",
            description = "이메일과 비밀번호를 사용하여 액세스 토큰과 리프레시 토큰을 발급합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "로그인 성공",
                                    value = "{\"success\": true, \"data\": {\"accessToken\": \"Bearer ey...\", \"refreshToken\": \"ey...\"}, \"message\": null}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "로그인 실패 (유저 없음)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "유저 없음",
                                    summary = "NOT_FOUND",
                                    value = "{\"code\": \"4040\", \"message\": \"존재하지 않는 사용자입니다.\", \"detail\": null}"
                            )
                    )
            )
    })
    @PostMapping("/login/owner")
    public ApiResponse<AuthResponseDto.TokenDto> ownerLogin(
            @Valid @RequestBody OwnerLoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponseDto.TokenDto tokenDto = authService.ownerLogin(request);

        response.setHeader("Authorization", "Bearer " + tokenDto.getAccessToken());

        return ApiResponse.ok(tokenDto);
    }

    @Operation(
            summary = "이메일 찾기",
            description = "휴대폰 인증 성공 후 발급된 **'인증 완료 증표'**를 확인하여 가입된 이메일을 반환합니다. \n\n" +
                    "**[주의]** 이 API 호출 전 반드시 `/verifications/sms/confirm`을 통해 인증 성공 기록을 생성해야 합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"success\": true, \"data\": \"seatnow@gmail.com\", \"message\": \"이메일 찾기에 성공하였습니다.\"}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 미완료 또는 시간초과",
                                    value = "{\"code\": \"4003\", \"message\": \"인증 시간이 만료되었습니다. 다시 시도해주세요.\", \"detail\": \"인증 성공 증표를 찾을 수 없습니다.\"}"
                            ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "유저 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\": \"4040\", \"message\": \"존재하지 않는 사용자입니다.\", \"detail\": null}"))
            )
    })
    @PostMapping("/find-email")
    public ApiResponse<String> findEmail(@Valid @RequestBody SmsSendRequest request) {
        String email = authService.findEmailByPhone(request.getPhoneNumber());
        return ApiResponse.ok(email, "이메일 찾기에 성공하였습니다.");
    }

    @Operation(
            summary = "비밀번호 찾기 (임시 비밀번호 발급)",
            description = "이메일 인증 성공 후 발급된 **'인증 완료 증표'**를 확인하여, 해당 계정의 비밀번호를 임시 비밀번호로 변경하고 메일로 발송합니다. \n\n" +
                    "**[주의]** 이 API 호출 전 반드시 `/verifications/email/confirm`을 통해 인증 성공 기록을 생성해야 합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "임시 비밀번호 발송 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"success\": true, \"data\": true, \"message\": \"입력하신 이메일로 임시 비밀번호가 발급되었습니다.\"}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 미완료 또는 시간초과",
                                    value = "{\"code\": \"4003\", \"message\": \"인증 시간이 만료되었습니다. 다시 시도해주세요.\", \"detail\": \"인증 성공 증표를 찾을 수 없습니다.\"}"
                            ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "유저 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "등록되지 않은 이메일",
                                    summary = "USER_NOT_FOUND",
                                    value = "{\"code\": \"4040\", \"message\": \"존재하지 않는 사용자입니다.\", \"detail\": \"null\"}"
                            ))
            )
    })
    @PostMapping("/find-password")
    public ApiResponse<Boolean> findPassword(@Valid @RequestBody EmailSendRequest request) {
        authService.sendTemporaryPassword(request.getEmail());
        return ApiResponse.ok(true, "입력하신 이메일로 임시 비밀번호가 발급되었습니다.");
    }
}
