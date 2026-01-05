package depth.main.seatnow.global.security.filter;

import depth.main.seatnow.global.exception.custom.UnauthorizedException;
import depth.main.seatnow.global.security.token.CustomUserDetailsService;
import depth.main.seatnow.global.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request); // 헤더에서 토큰 추출

        if (token != null) {
            try {
                jwtUtil.validateToken(token);

                String userId = jwtUtil.getUserId(token);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId);

                // 시큐리티 내부에 "이 사람 인증됨"이라고 기록
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (UnauthorizedException ignored) {

            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    }
}
