package depth.main.seatnow.domain.store.repository;

import depth.main.seatnow.domain.store.entity.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByBusinessNumber(String businessNumber);

    Optional<Store> findByUserId(Long userId);
}
