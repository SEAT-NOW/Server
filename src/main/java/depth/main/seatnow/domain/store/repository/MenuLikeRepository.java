package depth.main.seatnow.domain.store.repository;

import depth.main.seatnow.domain.store.entity.menu.Menu;
import depth.main.seatnow.domain.store.entity.menu.MenuLike;
import depth.main.seatnow.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuLikeRepository extends JpaRepository<MenuLike, Long> {
    Optional<MenuLike> findByUserAndMenu(User user, Menu menu);
}
