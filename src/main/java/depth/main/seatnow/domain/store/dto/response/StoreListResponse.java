package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.entity.store.StoreImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StoreListResponse {
    private Long storeId;
    private String storeName;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer totalSeatCount;
    private Integer availableSeatCount;
    private String seatStatus;
    private String mainImageUrl;
    private LocalDateTime updatedAt;
    private Double distance;

    public static StoreListResponse from(Store store, Double distance) {
        String imageUrl = store.getImages().stream()
                .filter(StoreImage::isMain)
                .findFirst()
                .map(StoreImage::getImageUrl)
                .orElse(null);

        return StoreListResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .totalSeatCount(store.getTotalSeatCount())
                .availableSeatCount(store.getAvailableSeatCount())
                .seatStatus(store.getStatusTag().name())
                .mainImageUrl(imageUrl)
                .updatedAt(store.getModifiedAt())
                .distance(distance)
                .build();
    }
}