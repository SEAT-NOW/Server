package depth.main.seatnow.user.login.repository;

import depth.main.seatnow.user.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialId(Long socialId);
}
