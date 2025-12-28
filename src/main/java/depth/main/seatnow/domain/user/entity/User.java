package depth.main.seatnow.domain.user.entity;

import depth.main.seatnow.domain.user.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long socialId;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public User(Long socialId, String nickname, Role role) {
        this.socialId = socialId;
        this.nickname = nickname;
        this.role = role;
    }
}
