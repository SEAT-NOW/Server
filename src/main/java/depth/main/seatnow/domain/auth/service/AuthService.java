package depth.main.seatnow.domain.auth.service;

import depth.main.seatnow.domain.auth.dto.request.OwnerLoginRequest;
import depth.main.seatnow.domain.auth.dto.response.AuthResponseDto;
import depth.main.seatnow.domain.auth.dto.response.KakaoDTO;
import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.repository.StoreRepository;
import depth.main.seatnow.domain.user.entity.User;
import depth.main.seatnow.domain.user.entity.enums.Role;
import depth.main.seatnow.domain.user.repository.RefreshTokenRepository;
import depth.main.seatnow.domain.user.repository.UserRepository;
import depth.main.seatnow.global.exception.custom.BadRequestException;
import depth.main.seatnow.global.exception.custom.NotFoundException;
import depth.main.seatnow.global.exception.custom.UnauthorizedException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import depth.main.seatnow.global.util.JwtUtil;
import depth.main.seatnow.infrastructure.external.kakao.KakaoAuthClient;
import depth.main.seatnow.infrastructure.external.kakao.KakaoUserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

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
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final StoreRepository storeRepository;

    @Value("${kakao.auth.client}")
    private String clientId;

    @Value("${kakao.auth.redirect}")
    private String redirectUri;

    /**
     * 카카오 로그인
     */
    @Transactional
    public AuthResponseDto.TokenDto kakaoLogin(String kakaoAccessToken) {
        // 1. 카카오 유저 정보 조회
        KakaoDTO.KakaoProfile kakaoProfile = kakaoUserClient.getUserInfo("Bearer " + kakaoAccessToken);

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

        Long storeId = storeRepository.findByUserId(user.getId())
                .map(Store::getId)
                .orElse(null);

        String accessToken = jwtUtil.createAccessToken(String.valueOf(user.getId()), user.getRole().toString());
        String refreshToken = jwtUtil.createRefreshToken(String.valueOf(user.getId()));

        return AuthResponseDto.TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .storeId(storeId)
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

    /**
     * 휴대폰 번호로 이메일 찾기
     */
    public String findEmailByPhone(String phoneNumber) {
        // 레디스에 인증 완료 증표가 있는지 확인
        String isCompleted = redisTemplate.opsForValue().get("sms_completed:" + phoneNumber);

        if (isCompleted == null) {
            throw new BadRequestException(ErrorCode.EXPIRED_VERIFICATION_CODE, "인증 성공 증표를 찾을 수 없습니다."); // 인증 안 됐거나 만료됨
        }

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        // 레디스에서 인증 완료 증표 삭제
        redisTemplate.delete("sms_completed:" + phoneNumber);

        return user.getEmail();
    }

    @Transactional
    public void sendTemporaryPassword(String email) {
        // 1. Redis에서 이메일 인증 완료 증표 확인
        String isCompleted = redisTemplate.opsForValue().get("email_completed:" + email);

        if (isCompleted == null) {
            throw new BadRequestException(ErrorCode.EXPIRED_VERIFICATION_CODE, "인증 성공 증표를 찾을 수 없습니다."); // 인증코드가 일치하지 않거나 만료됨
        }

        // 2. DB에서 이메일로 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        // 3. 임시 비밀번호 생성
        String tempPassword = generateRandomPassword();

        // 4. DB 반영 (암호화하여 기존 비밀번호 덮어쓰기)
        user.updatePassword(passwordEncoder.encode(tempPassword));

        // 5. 임시 비밀번호 메일 발송
        sendTempPasswordEmail(email, tempPassword);

        // 6. 사용한 증표 삭제
        redisTemplate.delete("email_completed:" + email);
    }

    /**
     * 메일로 임시 비밀번호 전송
     */
    private void sendTempPasswordEmail(String email, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[SeatNow] 임시 비밀번호 발급 안내");
        message.setText("안녕하세요. SeatNow입니다.\n\n" +
                "입력하신 이메일로 임시 비밀번호가 발급되었습니다.\n" +
                "임시 비밀번호: [" + tempPassword + "]\n\n" +
                "보안을 위해 로그인 후 마이페이지에서 반드시 비밀번호를 변경해 주세요.");
        mailSender.send(message);

    }

    /**
     * 랜덤 비밀번호 생성기(영문 대소문자 + 숫자 조합 10자리)
     */
    private String generateRandomPassword() {
        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(charSet.length());
            sb.append(charSet.charAt(index));
        }
        return sb.toString();
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