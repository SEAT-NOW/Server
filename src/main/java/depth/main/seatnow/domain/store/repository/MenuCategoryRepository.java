package depth.main.seatnow.domain.store.repository;

import depth.main.seatnow.domain.store.entity.menu.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
}
