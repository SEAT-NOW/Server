package depth.main.seatnow.user.login.dto;

import lombok.Builder;
import lombok.Getter;

public class AuthResponseDto {

    @Getter
    @Builder
    public static class TokenDto {
        private String accessToken;
    }
}
