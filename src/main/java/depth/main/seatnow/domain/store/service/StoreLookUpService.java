package depth.main.seatnow.domain.store.service;

import depth.main.seatnow.domain.store.dto.response.StoreListResponse;

import depth.main.seatnow.domain.store.dto.response.StoreDetailResponse;
import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.repository.StoreRepository;
import depth.main.seatnow.global.exception.custom.NotFoundException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreLookUpService {
    private final StoreRepository storeRepository;
    @Transactional
    public List<StoreListResponse> searchStores(String keyword, Double lat, Double lng, Double radius, Integer headCount) {
        List<Store> stores;

        // 목록 조회
        if (keyword != null && !keyword.isBlank()) {
            // 키워드 검색
            stores = storeRepository.searchByKeyword(keyword);
        } else if (lat != null && lng != null) {
            // 키워드가 없을 때만 위치 기반 반경 검색
            stores = storeRepository.searchByLocation(lat, lng, radius);
        } else {
            return List.of();
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

        return responseList;
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

    public StoreDetailResponse getStoreDetails(Long storeId) {
        Optional<Store> optionalStore = storeRepository.findById(storeId);

        if (optionalStore.isEmpty()) {
            throw new NotFoundException(ErrorCode.STORE_NOT_FOUND);
        }

        Store store = optionalStore.get();
        return StoreDetailResponse.from(store);
    }

}
