package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.seat.enums.SeatStatus;
import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.entity.store.StoreImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@Schema(description = "킵한 매장 목록 조회 응답")
public class KeptStoreListResponse {

    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "매장 이름", example = "명지대 꿈에도")
    private String storeName;

    @Schema(description = "대학 이름들", example = "명지대학교")
    private List<String> universityNames;

    @Schema(description = "매장 상태 태그 이름 (한글)", example = "혼잡")
    private SeatStatus statusTagName;

    @Schema(description = "총 좌석 수", example = "30")
    private Integer totalSeatCount;

    @Schema(description = "사용중인 좌석 수", example = "13")
    private Integer usedSeatCount;

    @Schema(description = "매장 대표 이미지(0번 인덱스가 대표 이미지)", example = "[\"url1.jpg\"")
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
                .statusTagName(store.getStatusTag())
                .totalSeatCount(store.getTotalSeatCount())
                .usedSeatCount(store.getUsedSeatCount())
                .images(image)
                .build();
    }

}
