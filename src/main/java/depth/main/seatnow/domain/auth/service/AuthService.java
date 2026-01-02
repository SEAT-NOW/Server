package depth.main.seatnow.domain.auth.service;

import depth.main.seatnow.domain.auth.dto.request.OwnerLoginRequest;
import depth.main.seatnow.global.exception.custom.NotFoundException;
import depth.main.seatnow.global.exception.custom.UnauthorizedException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import depth.main.seatnow.domain.auth.dto.response.KakaoDTO;
import depth.main.seatnow.domain.auth.dto.response.AuthResponseDto;
import depth.main.seatnow.domain.user.entity.User;
import depth.main.seatnow.domain.user.entity.enums.Role;
import depth.main.seatnow.infrastructure.external.kakao.KakaoAuthClient;
import depth.main.seatnow.infrastructure.external.kakao.KakaoUserClient;
import depth.main.seatnow.domain.user.repository.UserRepository;
import depth.main.seatnow.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoUserClient kakaoUserClient;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${kakao.auth.client}")
    private String clientId;

    @Value("${kakao.auth.redirect}")
    private String redirectUri;

    @Transactional
    public AuthResponseDto.TokenDto kakaoLogin(String code) {
        //카카오 토큰 발급
        KakaoDTO.OAuthToken oAuthToken = kakaoAuthClient.getOAuthToken(
                "authorization_code",
                clientId,
                redirectUri,
                code
        );

        //토큰에서 유저 info 받아오기
        KakaoDTO.KakaoProfile kakaoProfile = kakaoUserClient.getUserInfo(
                "Bearer " + oAuthToken.getAccess_token()
        );

        //로그인 or 회원가입
        Long socialId = kakaoProfile.getId();
        User user = userRepository.findBySocialId(socialId).orElseGet(() -> signup(kakaoProfile));

        return createTokenDto(user);
    }
    @Transactional
    public AuthResponseDto.TokenDto ownerLogin(OwnerLoginRequest request) {
        // 1. 이메일로 유저 찾기 (UserRepository 활용)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        // 2. PasswordEncoder로 비번 대조
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }

        return createTokenDto(user);
    }

    //토큰 재발급(access,refresh 토큰 둘다 재발급)
    @Transactional
    public AuthResponseDto.TokenDto reissue(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
        }

        String userId = jwtUtil.getUserId(refreshToken);
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        return createTokenDto(user);
    }

    // 토큰 발급 로직
    private AuthResponseDto.TokenDto createTokenDto(User user) {
        String accessToken = jwtUtil.createAccessToken(String.valueOf(user.getId()), user.getRole().toString());
        String refreshToken = jwtUtil.createRefreshToken(String.valueOf(user.getId()));

        return AuthResponseDto.TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    private User signup(KakaoDTO.KakaoProfile kakaoProfile) {
        User newUser = User.builder()
                .socialId(kakaoProfile.getId())
                .nickname(kakaoProfile.getKakao_account().getProfile().getNickname())
                .role(Role.USER)
                .build();
        return userRepository.save(newUser);
    }


}
