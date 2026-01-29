package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.seat.enums.SeatStatus;
import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.entity.store.StoreImage;
import lombok.Builder;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
public class KeptStoreListResponse {

    private Long storeId;
    private String storeName;
    private List<String> universityNames;
    private SeatStatus statusTag;
    private Integer totalSeatCount;
    private Integer usedSeatCount;
    private String images;

    public static KeptStoreListResponse from(Store store) {
        Optional<String> mainImageUrl = store.getImages().stream()
                .sorted(Comparator.comparing(StoreImage::isMain).reversed())
                .map(StoreImage::getImageUrl)
                .findFirst();

        String image = null;
        if (mainImageUrl.isPresent()) {
            image = mainImageUrl.get();
        }

        return KeptStoreListResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .universityNames(store.getUniversityNames())
                .statusTag(store.getStatusTag())
                .totalSeatCount(store.getTotalSeatCount())
                .usedSeatCount(store.getUsedSeatCount())
                .images(image)
                .build();
    }

}
