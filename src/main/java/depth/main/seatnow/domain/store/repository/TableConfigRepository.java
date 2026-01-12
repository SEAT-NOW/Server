package depth.main.seatnow.domain.store.repository;

import depth.main.seatnow.domain.store.entity.seat.TableConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableConfigRepository extends JpaRepository<TableConfig, Long> {
}
