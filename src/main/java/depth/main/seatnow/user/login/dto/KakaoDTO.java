package depth.main.seatnow.user.login.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class KakaoDTO {

    @Getter
    @NoArgsConstructor
    public static class OAuthToken {
        private String access_token;
        private String token_type;
        private String refresh_token;
        private int expires_in;
        private String scope;
        private int refresh_expires_in;
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
