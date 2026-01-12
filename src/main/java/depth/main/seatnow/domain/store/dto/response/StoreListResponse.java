package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.entity.store.StoreImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<String> images;
    private LocalDateTime updatedAt;
    private Double distance;

    public static StoreListResponse from(Store store, Double distance) {
        List<String> imageUrls = store.getImages().stream()
                .map(StoreImage::getImageUrl)
                .limit(3)
                .toList();

        return StoreListResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .totalSeatCount(store.getTotalSeatCount())
                .availableSeatCount(store.getAvailableSeatCount())
                .seatStatus(store.getStatusTag().name())
                .images(imageUrls)
                .updatedAt(store.getModifiedAt())
                .distance(distance)
                .build();
    }
}