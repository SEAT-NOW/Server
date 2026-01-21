package depth.main.seatnow.domain.store.repository;

import depth.main.seatnow.domain.store.entity.menu.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
