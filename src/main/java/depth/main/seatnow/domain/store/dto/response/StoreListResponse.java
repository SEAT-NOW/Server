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

    @Schema(description = "읍면동 단위 지역명", example = "역북동")
    private String neighborhood;

    @Schema(description = "위도", example = "37.556")
    private Double latitude;

    @Schema(description = "경도", example = "126.924")
    private Double longitude;

    @Schema(description = "총 좌석 수", example = "30")
    private Integer totalSeatCount;

    @Schema(description = "현재 이용 가능 좌석 수", example = "10")
    private Integer availableSeatCount;

    @Schema(description = "매장 상태 태그 (영문)", example = "CROWDED")
    private String statusTag;

    @Schema(description = "매장 상태 태그 이름 (한글)", example = "혼잡")
    private String statusTagName;

    @Schema(description = "매장 이미지 리스트 (최대 3장, 0번 인덱스가 대표 이미지)", example = "[\"url1.jpg\", \"url2.jpg\"]")
    private List<String> images;

    @Schema(description = "정보 수정 일시", example = "2024-01-12T10:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "사용자 위치와의 거리", example = "300m 또는 1.2km")
    private String distance;

    @Schema(description = "현재 영업 상태 (영업 중, 곧 영업 종료, 영업 종료)", example = "영업 중")
    private String operationStatus;

    @Schema(description = "매장 전화번호", example = "021234567")
    private String storePhone;

    public static StoreListResponse from(Store store, String distance) {
        List<String> imageUrls = store.getImages().stream()
                .sorted(Comparator.comparing(StoreImage::isMain).reversed())
                .map(StoreImage::getImageUrl)
                .limit(3)
                .toList();

        return StoreListResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .neighborhood(store.getNeighborhood())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .totalSeatCount(store.getTotalSeatCount())
                .availableSeatCount(store.getAvailableSeatCount())
                .statusTag(store.getStatusTag().name())
                .statusTagName(store.getStatusTag().getDescription())
                .images(imageUrls)
                .updatedAt(store.getModifiedAt())
                .distance(distance)
                .operationStatus(store.getOperationStatus().getDescription())
                .storePhone(store.getStorePhone())
                .build();
    }
}