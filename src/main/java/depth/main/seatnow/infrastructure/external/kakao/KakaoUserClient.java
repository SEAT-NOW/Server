package depth.main.seatnow.infrastructure.external.kakao;

import depth.main.seatnow.domain.auth.dto.response.KakaoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "kakaoUserClient", url = "https://kapi.kakao.com")
public interface KakaoUserClient {

    @GetMapping(value = "/v2/user/me", consumes = "application/x-www-form-urlencoded")
    KakaoDTO.KakaoProfile getUserInfo(
            @RequestHeader("Authorization") String accessToken
    );
}
