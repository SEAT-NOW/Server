package depth.main.seatnow.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class AuthResponseDto {

    @Getter
    @Builder
    @Schema(description = "토큰 응답 객체")
    public static class TokenDto {
        @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1...")
        private String accessToken;

        @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1...")
        private String refreshToken;
    }
}
