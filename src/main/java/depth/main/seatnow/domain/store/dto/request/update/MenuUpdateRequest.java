package depth.main.seatnow.domain.store.dto.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "메뉴 등록 및 수정 요청")
public class MenuUpdateRequest {
    @Schema(description = "메뉴 ID (신규 등록 시 null, 수정 시 필수)", example = "1")
    private Long id;

    @Schema(description = "메뉴명", example = "치즈 닭갈비")
    private String name;

    @Schema(description = "가격", example = "18000")
    private Integer price;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    @Schema(description = "소속 카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "기존 이미지 유지 여부 (수정 시 사진을 바꾸지 않을 때 true)", example = "true")
    private boolean keepImage;
}
