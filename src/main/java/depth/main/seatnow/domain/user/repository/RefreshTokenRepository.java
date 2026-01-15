package depth.main.seatnow.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;

    public void save(String userId, String refreshToken) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();

        values.set(userId, refreshToken, 1209600, TimeUnit.SECONDS);//14일
    }

    public Optional<String> findById(String userId) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        String token = values.get(userId);

        return Optional.ofNullable(token);
    }

    public void delete(String userId) {
        redisTemplate.delete(userId);
    }
}
