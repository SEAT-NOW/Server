package depth.main.seatnow.domain.auth.service;

import depth.main.seatnow.domain.auth.dto.request.OwnerLoginRequest;
import depth.main.seatnow.domain.auth.dto.response.AuthResponseDto;
import depth.main.seatnow.domain.auth.dto.response.KakaoDTO;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

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
     * 사장님 로그인
     */
    @Transactional
    public AuthResponseDto.TokenDto ownerLogin(OwnerLoginRequest request) {
        // 1. 이메일로 유저 찾기 (UserRepository 활용)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.NOT_FOUND));

        // 2. PasswordEncoder로 비번 대조
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(ErrorCode.NOT_FOUND);
        }

        String accessToken = jwtUtil.createAccessToken(String.valueOf(user.getId()), user.getRole().toString());
        String refreshToken = jwtUtil.createRefreshToken(String.valueOf(user.getId()));

        return AuthResponseDto.TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    public AuthResponseDto.TokenDto reissue(String refreshToken) {
        // 1. 토큰 검증 및 userId 추출
        String userId = validateToken(refreshToken);

        // 2. 유저 조회
        User user = findUser(userId);

        // 3. 토큰 생성 및 저장
        return createAndSaveTokens(user);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.delete(String.valueOf(userId));
    }

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
        String userId = String.valueOf(user.getId());

        String accessToken = jwtUtil.createAccessToken(userId, user.getRole().toString());
        String refreshToken = jwtUtil.createRefreshToken(userId);

        refreshTokenRepository.save(userId, refreshToken);

        return AuthResponseDto.TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String validateToken(String refreshToken) {
        jwtUtil.validateToken(refreshToken);
        String userId = jwtUtil.getUserId(refreshToken);

        String storedToken = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!storedToken.equals(refreshToken)) {
            throw new UnauthorizedException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        return userId;
    }

    private User findUser(String userId) {
        return userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
    }
}