package depth.main.seatnow.global.security;

import depth.main.seatnow.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public Long getUserId() {
        return user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return String.valueOf(user.getId());
    }

    @Override
    public boolean isAccountNonExpired() { return true; } // 계정 만료 여부

    @Override
    public boolean isAccountNonLocked() { return true; } // 계정 잠금 여부

    @Override
    public boolean isCredentialsNonExpired() { return true; } // 비밀번호 만료 여부

    @Override
    public boolean isEnabled() { return true; } // 계정 활성화 여부
}
