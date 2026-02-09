package depth.main.seatnow.domain.store.repository;

import depth.main.seatnow.domain.store.entity.menu.Menu;
import depth.main.seatnow.domain.store.entity.menu.MenuLike;
import depth.main.seatnow.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuLikeRepository extends JpaRepository<MenuLike, Long> {
    Optional<MenuLike> findByUserAndMenu(User user, Menu menu);

    @Query("SELECT ml.menu.id FROM MenuLike ml " +
            "WHERE ml.user.id = :userId AND ml.menu.menuCategory.store.id = :storeId")
    List<Long> findLikedMenuIds(@Param("userId") Long userId, @Param("storeId") Long storeId);
}
