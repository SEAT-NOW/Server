package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.entity.store.StoreImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@Builder
@Schema(description = "매장 목록 조회 응답 (검색, 거리순 조회, 죄석수 조회)")
public class StoreListResponse {
    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "매장 이름", example = "명지대 꿈에도")
    private String storeName;

    @Schema(description = "매장 주소", example = "경기도 용인시 명지로 113")
    private String address;

    @Schema(description = "위도", example = "37.556")
    private Double latitude;

    @Schema(description = "경도", example = "126.924")
    private Double longitude;

    @Schema(description = "총 좌석 수", example = "30")
    private Integer totalSeatCount;

    @Schema(description = "현재 이용 가능 좌석 수", example = "10")
    private Integer availableSeatCount;

    @Schema(description = "좌석 혼잡도 상태 (여유, 보통, 혼잡, 만석)", example = "여유")
    private String statusTag;
    private String statusTagName;

    @Schema(description = "매장 이미지 리스트 (최대 3장, 0번 인덱스가 대표 이미지)", example = "[\"url1.jpg\", \"url2.jpg\"]")
    private List<String> images;

    @Schema(description = "정보 수정 일시", example = "2024-01-12T10:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "사용자 위치와의 거리", example = "300m 또는 1.2km")
    private String distance;

    @Schema(description = "현재 영업 상태 (영업 중, 곧 영업 종료, 영업 종료)", example = "영업 중")
    private String operationStatus;

    public static StoreListResponse from(Store store, String distance) {
        List<String> imageUrls = store.getImages().stream()
                .sorted(Comparator.comparing(StoreImage::isMain).reversed())
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
                .statusTag(store.getOperationStatus().name())
                .statusTagName(store.getOperationStatus().getDescription())
                .images(imageUrls)
                .updatedAt(store.getModifiedAt())
                .distance(distance)
                .operationStatus(store.getOperationStatus().getDescription())
                .build();
    }
}