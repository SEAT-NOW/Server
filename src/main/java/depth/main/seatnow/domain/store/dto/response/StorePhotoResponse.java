package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.store.StoreImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "매장 사진 조회 응답")
public class StorePhotoResponse {
    @Schema(description = "매장 사진 정보 리스트")
    private List<PhotoDto> storeImages;

    public static StorePhotoResponse of(List<StoreImage> images) {
        return StorePhotoResponse.builder()
                .storeImages(images.stream()
                        .map(PhotoDto::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "개별 사진 상세 정보")
    public static class PhotoDto {
        @Schema(description = "사진 ID", example = "101")
        private Long id;

        @Schema(description = "이미지 URL", example = "https://s3.../image.jpg")
        private String imageUrl;

        @Schema(description = "대표 이미지 여부", example = "true")
        private boolean isMain;

        public static PhotoDto from(StoreImage entity) {
            return PhotoDto.builder()
                    .id(entity.getId())
                    .imageUrl(entity.getImageUrl())
                    .isMain(entity.isMain())
                    .build();
        }
    }
}
