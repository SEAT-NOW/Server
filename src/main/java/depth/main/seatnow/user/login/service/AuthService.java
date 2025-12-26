package depth.main.seatnow.user.login.service;

import depth.main.seatnow.user.login.dto.KakaoDTO;
import depth.main.seatnow.user.login.dto.AuthResponseDto;
import depth.main.seatnow.user.login.entity.User;
import depth.main.seatnow.user.login.entity.enums.Role;
import depth.main.seatnow.user.login.infrastructure.KakaoAuthClient;
import depth.main.seatnow.user.login.infrastructure.KakaoUserClient;
import depth.main.seatnow.user.login.repository.UserRepository;
import depth.main.seatnow.user.login.util.JwtUtil;
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

    @Value("${spring.kakao.auth.client}")
    private String clientId;

    @Value("${spring.kakao.auth.redirect}")
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

        //jwt 토큰 만들기(자체 토큰)
        String accessToken = jwtUtil.createAccessToken(String.valueOf(user.getSocialId()), user.getRole().toString());

        return AuthResponseDto.TokenDto.builder()
                .accessToken(accessToken)
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
