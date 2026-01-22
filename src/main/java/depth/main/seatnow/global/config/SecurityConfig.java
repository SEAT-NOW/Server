package depth.main.seatnow.global.config;

import depth.main.seatnow.global.security.filter.JwtAuthenticationFilter;
import depth.main.seatnow.global.security.handler.JwtAccessDeniedHandler;
import depth.main.seatnow.global.security.handler.JwtAuthenticationEntryPoint;
import depth.main.seatnow.global.security.token.CustomUserDetailsService;
import depth.main.seatnow.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private static final String[] WHITE_LIST = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/auth/**",
            "/login/**",
            "/api/v1/auth/verifications/**",
            "/api/v1/places/**",
            "/api/v1/images/upload",
            "/api/v1/stores/owner/signup",
            "/api/v1/stores/search"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stores/{storeId}/seats").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/stores/seats").hasRole("OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/stores/owner").hasRole("OWNER")
                        .requestMatchers(HttpMethod.POST,"/api/v1/stores/owner/verify-password").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PATCH,"/api/v1/stores/owner/password").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PATCH,"/api/v1/stores/operation/phone-number").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PATCH,"/api/v1/stores/layout").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PATCH,"/api/v1/stores/operation").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PATCH,"/api/v1/stores/operation/images").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/stores/menu/categories").hasRole("OWNER")
                        .requestMatchers(HttpMethod.POST,"/api/v1/stores/menus/details").hasRole("OWNER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, customUserDetailsService),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
