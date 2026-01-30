package depth.main.seatnow.domain.store.repository;

import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.entity.store.StoreKeep;
import depth.main.seatnow.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreKeepRepository extends JpaRepository<StoreKeep, Long> {

    Optional<StoreKeep> findByUserAndStore(User user, Store store);

    boolean existsByUserAndStore(User user, Store store);

    List<StoreKeep> findAllByUserOrderByCreatedAtDesc(User user);
}
