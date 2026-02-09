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
    private List<CategoryOrderDto> categoryOrders;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryOrderDto {
        private Long categoryId;    // 카테고리 ID
        private List<Long> menuIds; // 해당 카테고리 내의 정렬된 메뉴 ID들
    }
}
