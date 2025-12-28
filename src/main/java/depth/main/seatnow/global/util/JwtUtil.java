package depth.main.seatnow.global.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final Key key;
    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;

    public JwtUtil(@Value("${jwt.secret}") String secretKey,
                   @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityTime,
                   @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityTime) {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValiditySeconds = accessTokenValidityTime * 10000;
        this.refreshTokenValiditySeconds = refreshTokenValidityTime * 10000;
    }

    public String createAccessToken(String socialId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(socialId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValiditySeconds))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String socialId) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(socialId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValiditySeconds))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //토큰 유효성 검증(token 재발급 받을때)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    public String getSocialId(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
