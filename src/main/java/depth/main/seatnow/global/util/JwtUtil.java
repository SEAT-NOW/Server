package depth.main.seatnow.global.util;

import depth.main.seatnow.global.exception.custom.UnauthorizedException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;
    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;

    public JwtUtil(@Value("${jwt.secret}") String secretKey,
                   @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityTime,
                   @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityTime) {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValiditySeconds = accessTokenValidityTime * 1000;
        this.refreshTokenValiditySeconds = refreshTokenValidityTime * 1000;
    }

    public String createAccessToken(String userId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValiditySeconds))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String userId) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValiditySeconds))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //토큰 유효성 검증(token 재발급 받을때)
    public void validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (SecurityException | UnsupportedJwtException | MalformedJwtException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorCode.EXPIRED_TOKEN);
        }
    }

    public String getUserId(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (Exception e) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
        }
    }
}
