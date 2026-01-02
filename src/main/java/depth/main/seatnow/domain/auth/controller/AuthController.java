package depth.main.seatnow.domain.auth.controller;

import depth.main.seatnow.domain.auth.dto.request.OwnerLoginRequest;
import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.domain.auth.dto.response.AuthResponseDto;
import depth.main.seatnow.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
