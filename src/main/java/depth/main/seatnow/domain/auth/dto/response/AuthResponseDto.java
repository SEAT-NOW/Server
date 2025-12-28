package depth.main.seatnow.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

public class AuthResponseDto {

    @Getter
    @Builder
    public static class TokenDto {
        private String accessToken;
        private String refreshToken;
    }
}
