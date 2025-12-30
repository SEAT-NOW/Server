package depth.main.seatnow.domain.user.entity;

import depth.main.seatnow.domain.user.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long socialId; // 카카오용

    private String nickname; // 카카오용

    private String email; // 사장님용

    private String password; // 사장님용

    private String phoneNumber; // 사장님용

    @Enumerated(EnumType.STRING)
    private Role role;


}
