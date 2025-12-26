package depth.main.seatnow.user.login.controller;

import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.user.login.dto.AuthResponseDto;
import depth.main.seatnow.user.login.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/auth/login/kakao")
    public ApiResponse<AuthResponseDto.TokenDto> kakaoLogin(@RequestParam("code") String code, HttpServletResponse response) {
        // 1. 서비스 호출 (토큰만 받아옴)
        AuthResponseDto.TokenDto tokenDto = authService.kakaoLogin(code);

        // 헤더에 jwt 토큰 세팅
        response.setHeader("Authorization", "Bearer " + tokenDto.getAccessToken());

        return ApiResponse.ok(tokenDto);
    }
}
