package depth.main.seatnow.domain.store.repository;

import depth.main.seatnow.domain.store.entity.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByBusinessNumber(String businessNumber);

    // 내 주변 반경 N km 검색
    @Query(value = "SELECT * FROM store " +
            "WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(latitude)))) <= :radius " +
            "ORDER BY seat_modified_at DESC", nativeQuery = true)
    List<Store> searchByLocation(@Param("lat") double lat, @Param("lng") double lng, @Param("radius") double radius);

    // 키워드 검색
    @Query("SELECT s FROM Store s " +
            "WHERE s.storeName LIKE CONCAT('%', :keyword, '%') " +
            "OR s.address LIKE CONCAT('%', :keyword, '%') " +
            "OR s.neighborhood LIKE CONCAT('%', :keyword, '%') " +
            "ORDER BY s.modifiedAt DESC")
    List<Store> searchByKeyword(@Param("keyword") String keyword);

    // 특정 대학교 이름을 가진 술집들 검색
    @Query("SELECT DISTINCT s FROM Store s " +
            "JOIN s.storeUniversities su " +
            "JOIN su.universityMaster um " +
            "WHERE um.name = :universityName " +
            "ORDER BY s.seatModifiedAt DESC")
    List<Store> findByUniversityName(@Param("universityName") String universityName);

    Optional<Store> findByUserId(Long userId);
}
