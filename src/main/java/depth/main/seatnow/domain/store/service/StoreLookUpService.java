package depth.main.seatnow.domain.store.service;

import depth.main.seatnow.domain.store.dto.response.StoreListResponse;

import depth.main.seatnow.domain.store.dto.response.StoreDetailResponse;
import depth.main.seatnow.domain.store.dto.response.StoreSearchResponse;
import depth.main.seatnow.domain.store.entity.menu.Menu;
import depth.main.seatnow.domain.store.entity.menu.MenuCategory;
import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.repository.MenuLikeRepository;
import depth.main.seatnow.domain.store.repository.StoreKeepRepository;
import depth.main.seatnow.domain.store.repository.StoreRepository;
import depth.main.seatnow.domain.store.repository.UniversityMasterRepository;
import depth.main.seatnow.domain.user.entity.User;
import depth.main.seatnow.domain.store.entity.university.UniversityMaster;
import depth.main.seatnow.domain.user.repository.UserRepository;
import depth.main.seatnow.global.exception.custom.NotFoundException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreLookUpService {

    private final StoreRepository storeRepository;
    private final StoreKeepRepository storeKeepRepository;
    private final UserRepository userRepository;
    private final UniversityMasterRepository universityMasterRepository;
    private final MenuLikeRepository menuLikeRepository;


    @Transactional
    public StoreSearchResponse searchStores(String keyword, String universityName, Double lat, Double lng, Double radius, Integer headCount) {
        List<Store> stores;
        List<String> relatedUniversities = new ArrayList<>();

        // 목록 조회
        if (universityName != null && !universityName.isBlank()) {
            // 대학 필터 클릭 시
            stores = storeRepository.findByUniversityName(universityName);
        }else if (keyword != null && !keyword.isBlank()) {
            // 키워드 검색
            stores = storeRepository.searchByKeyword(keyword);

            // 연관 대학 추출
            relatedUniversities = universityMasterRepository.findByNameContaining(keyword)
                    .stream()
                    .map(UniversityMaster::getName)
                    .toList();
        } else if (lat != null && lng != null) {
            // 키워드가 없을 때만 위치 기반 반경 검색
            stores = storeRepository.searchByLocation(lat, lng, radius);
        } else {
            return StoreSearchResponse.of(List.of(), List.of());
        }

        // 인원수 필터링
        // headCount가 1 이상일 때만 남은 좌석 수 비교
        if (headCount != null && headCount > 0) {
            List<Store> filteredStores = new ArrayList<>();
            for (Store store : stores) {
                if (store.getAvailableSeatCount() >= headCount) {
                    filteredStores.add(store);
                }
            }
            stores = filteredStores;
        }

        List<StoreListResponse> responseList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Store store : stores) {
            String distanceStr = null;
            store.updateOperationStatus(now);

            if (lat != null && lng != null) {
                double distanceMeters = calculateDistance(lat, lng, store.getLatitude(), store.getLongitude());
                distanceStr = formatDistance(distanceMeters);
            }

            responseList.add(StoreListResponse.from(store, distanceStr));
        }

        return StoreSearchResponse.of(responseList, relatedUniversities);
    }



    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구의 반지름 (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000; // km를 m로 변환
    }

    private String formatDistance(double meters) {
        if (meters < 1000) {
            return String.format("%.0fm", meters);
        } else {
            return String.format("%.1fkm", meters / 1000.0);
        }
    }

    public StoreDetailResponse getStoreDetails(Long storeId, Long userId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        store.updateOperationStatus(LocalDateTime.now()); //상세 조회 전 매장 업데이트

        boolean isKept = false;
        List<Long> likedMenuIds = new ArrayList<>();

        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
            isKept = storeKeepRepository.existsByUserAndStore(user, store);

            likedMenuIds = menuLikeRepository.findLikedMenuIds(userId, storeId);
        }

        List<Long> bestMenuIds = getBestMenuIds(store);

        return StoreDetailResponse.from(store, isKept, bestMenuIds, likedMenuIds);
    }

    /**
     * 좋아요 메뉴 가져오기
     * 조건: 좋아요 5개 이상, 상위 4개의 메뉴만, 좋아요 많은 순 정렬
     */
    @NotNull
    private static List<Long> getBestMenuIds(Store store) {

        List<Menu> bestMenuList = new ArrayList<>();

        // 좋아요 5개 이상 메뉴만 선별
        for (MenuCategory menuCategory : store.getMenuCategories()) {
            for (Menu menu : menuCategory.getMenus()) {
                if (menu.getLikeCount() >= 5) {
                    bestMenuList.add(menu);
                }
            }
        }

        // 내림차순
        bestMenuList.sort(new Comparator<Menu>() {
            @Override
            public int compare(Menu o1, Menu o2) {
                return o2.getLikeCount().compareTo(o1.getLikeCount());
            }
        });

        List<Long> result = new ArrayList<>();
        int count = 0;

        // 상위 4개 메뉴만 뽑아내기
        for (Menu menu : bestMenuList) {
            if (count >= 4) {
                break;
            }

            result.add(menu.getId());
            count++;
        }

        return result;
    }

}
