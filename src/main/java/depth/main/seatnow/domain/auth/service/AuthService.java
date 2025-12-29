package depth.main.seatnow.domain.auth.service;

import depth.main.seatnow.domain.auth.dto.response.AuthResponseDto;
import depth.main.seatnow.domain.auth.dto.response.KakaoDTO;
import depth.main.seatnow.domain.user.entity.RefreshToken;
import depth.main.seatnow.domain.user.entity.User;
import depth.main.seatnow.domain.user.entity.enums.Role;
import depth.main.seatnow.domain.user.repository.RefreshTokenRepository;
import depth.main.seatnow.domain.user.repository.UserRepository;
import depth.main.seatnow.global.exception.custom.NotFoundException;
import depth.main.seatnow.global.exception.custom.UnauthorizedException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import depth.main.seatnow.global.util.JwtUtil;
import depth.main.seatnow.infrastructure.external.kakao.KakaoAuthClient;
import depth.main.seatnow.infrastructure.external.kakao.KakaoUserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${kakao.auth.client}")
    private String clientId;

    @Value("${kakao.auth.redirect}")
    private String redirectUri;

    /**
     * 카카오 로그인
     */
    @Transactional
    public AuthResponseDto.TokenDto kakaoLogin(String code) {
        // 1. 카카오 토큰 및 유저 정보 조회
        KakaoDTO.KakaoProfile kakaoProfile = getKakaoProfile(code);

        // 2. 회원가입 or 로그인 (유저 조회/생성)
        User user = getOrCreateUser(kakaoProfile);

        // 3. 앱 토큰(Access, Refresh) 생성 및 저장
        return createAndSaveTokens(user);
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    public AuthResponseDto.TokenDto reissue(String refreshToken) {
        // 1. 토큰 유효성 검증 및 저장된 토큰 조회
        RefreshToken storedToken = validateAndGetStoredToken(refreshToken);

        // 2. 유저 조회
        User user = getUser(storedToken.getKey());

        // 3. 토큰 교체 (Access, Refresh 둘 다 갱신)
        return rotateTokens(storedToken, user);
    }

    // =====================================
    //  kakaoLogin 관련 메서드
    // =====================================

    private KakaoDTO.KakaoProfile getKakaoProfile(String code) {
        KakaoDTO.OAuthToken oAuthToken = kakaoAuthClient.getOAuthToken(
                "authorization_code",
                clientId,
                redirectUri,
                code
        );
        return kakaoUserClient.getUserInfo("Bearer " + oAuthToken.getAccess_token());
    }

    private User getOrCreateUser(KakaoDTO.KakaoProfile kakaoProfile) {
        return userRepository.findBySocialId(kakaoProfile.getId())
                .orElseGet(() -> signup(kakaoProfile));
    }

    private User signup(KakaoDTO.KakaoProfile kakaoProfile) {
        User newUser = User.builder()
                .socialId(kakaoProfile.getId())
                .nickname(kakaoProfile.getKakao_account().getProfile().getNickname())
                .role(Role.USER)
                .build();
        return userRepository.save(newUser);
    }

    private AuthResponseDto.TokenDto createAndSaveTokens(User user) {
        String accessToken = jwtUtil.createAccessToken(String.valueOf(user.getSocialId()), user.getRole().toString());
        String refreshToken = jwtUtil.createRefreshToken(String.valueOf(user.getSocialId()));

        // 이미 있으면 가져오고, 없으면 새로 빌더로 생성
        RefreshToken rt = refreshTokenRepository.findByKey(String.valueOf(user.getSocialId()))
                .orElse(RefreshToken.builder()
                        .key(String.valueOf(user.getSocialId()))
                        .value(refreshToken)
                        .build());

        rt.updateValue(refreshToken);
        refreshTokenRepository.save(rt);

        return AuthResponseDto.TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // =====================================
    //  reissue 관련 메서드
    // =====================================

    private RefreshToken validateAndGetStoredToken(String token) {
        jwtUtil.validateToken(token);

        String socialId = jwtUtil.getSocialId(token);

        RefreshToken storedToken = refreshTokenRepository.findByKey(socialId)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!storedToken.getValue().equals(token)) {
            throw new UnauthorizedException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        return storedToken;
    }

    private User getUser(String socialId) {
        return userRepository.findBySocialId(Long.parseLong(socialId))
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
    }

    private AuthResponseDto.TokenDto rotateTokens(RefreshToken storedToken, User user) {
        String accessToken = jwtUtil.createAccessToken(String.valueOf(user.getSocialId()), user.getRole().toString());
        String refreshToken = jwtUtil.createRefreshToken(String.valueOf(user.getSocialId()));

        storedToken.updateValue(refreshToken);

        return AuthResponseDto.TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}