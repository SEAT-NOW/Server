package depth.main.seatnow.domain.store.dto.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메뉴 순서 변경 요청")
public class MenuOrderUpdate {
    @Schema(description = "카테고리별 메뉴 정렬 정보 리스트")
    private List<CategoryOrderDto> categoryOrders;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "특정 카테고리 내 메뉴 순서 정보")
    public static class CategoryOrderDto {
        @Schema(description = "카테고리 ID", example = "7")
        private Long categoryId;    // 카테고리 ID

        @Schema(description = "순서대로 정렬된 메뉴 ID 리스트", example = "[1, 4, 2]")
        private List<Long> menuIds; // 해당 카테고리 내의 정렬된 메뉴 ID들
    }
}
