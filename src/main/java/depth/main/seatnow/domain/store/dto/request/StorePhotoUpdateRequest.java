package depth.main.seatnow.domain.store.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "매장 사진 일괄 수정 요청")
@Getter
@NoArgsConstructor
public class StorePhotoUpdateRequest {
    @Schema(description = "유지할 기존 이미지 리스트 (여기에 없는 ID는 삭제됨)")
    private List<ExistingImageDto> existingImages;

    @Getter @NoArgsConstructor
    public static class ExistingImageDto {
        @Schema(description = "이미지 ID", example = "1")
        private Long id;

        @Schema(description = "대표 사진 설정 여부", example = "true")
        @JsonProperty("isMain")
        private boolean isMain;
    }
}
