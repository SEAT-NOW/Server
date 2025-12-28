package depth.main.seatnow.domain.auth.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class KakaoDTO {

    @Getter
    @NoArgsConstructor
    public static class OAuthToken {
        private String access_token;
    }

    @Getter
    @NoArgsConstructor
    public static class KakaoProfile {
        private Long id;
        private KakaoAccount kakao_account;

        @Getter
        @NoArgsConstructor
        public static class KakaoAccount {
            private Profile profile;

            @Getter
            @NoArgsConstructor
            public static class Profile {
                private String nickname;
            }
        }
    }
}
