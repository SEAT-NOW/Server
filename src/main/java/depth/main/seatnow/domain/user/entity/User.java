package depth.main.seatnow.domain.user.entity;

import depth.main.seatnow.domain.store.entity.menu.MenuLike;
import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.entity.store.StoreKeep;
import depth.main.seatnow.domain.user.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuLike> menuLikes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreKeep> storeKeeps = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Store store;


    public void updatePassword(String encode) {
        this.password = encode;
    }
}
