package depth.main.seatnow.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@Getter
@NoArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 1209600) //14일
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    private String id;

    private String refreshToken;

    @Builder
    public RefreshToken(String id, String refreshToken) {
        this.id = id;
        this.refreshToken = refreshToken;
    }

    public void updateRefreshToken(String token) {
        this.refreshToken = token;
    }
}
