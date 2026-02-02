package depth.main.seatnow.domain.store.repository;

import depth.main.seatnow.domain.store.entity.university.UniversityMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UniversityMasterRepository extends JpaRepository<UniversityMaster, Long> {
    // 이름으로 대학교 찾기 (회원가입 시 중복 체크용)
    Optional<UniversityMaster> findByName(String name);

    // 검색어 자동완성용 (돋보기 리스트용)
    // "홍" ->  "홍익대학교"가 나오게 해줌
    List<UniversityMaster> findByNameContaining(String keyword);
}
