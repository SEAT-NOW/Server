package depth.main.seatnow.domain.auth.controller;

import depth.main.seatnow.domain.auth.dto.request.OwnerLoginRequest;
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
            @RequestParam("code") String code,
            HttpServletResponse response
    ) {
        //서비스 호출 (토큰만 받아옴)
        AuthResponseDto.TokenDto tokenDto = authService.kakaoLogin(code);

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
}
