package depth.main.seatnow.domain.store.dto.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메뉴 카테고리 일괄 수정 요청")
public class MenuCategoryUpdateRequest {
    @Schema(description = "카테고리 수정 리스트")
    private List<CategoryDto> categories;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDto {
        @Schema(description = "카테고리 ID (신규 추가 시 null)", example = "1")
        private Long id;

        @Schema(description = "카테고리명", example = "추천 안주")
        private String name;
    }
}
