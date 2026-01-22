package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.menu.Menu;
import depth.main.seatnow.domain.store.entity.menu.MenuCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "매장 메뉴 정보 조회 응답")
public class StoreMenuResponse {
    @Schema(description = "메뉴 카테고리 및 메뉴 리스트")
    private List<CategoryDto> categories;

    public static StoreMenuResponse of(List<MenuCategory> categories) {
        return StoreMenuResponse.builder()
                .categories(categories.stream()
                        .map(CategoryDto::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "메뉴 카테고리 상세 정보")
    public static class CategoryDto {
        @Schema(description = "카테고리 ID", example = "1")
        private Long id;

        @Schema(description = "카테고리 이름", example = "안주류")
        private String name;

        @Schema(description = "해당 카테고리의 메뉴 리스트")
        private List<MenuDto> menus;

        public static CategoryDto from(MenuCategory entity) {
            return CategoryDto.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .menus(entity.getMenus().stream()
                            .map(MenuDto::from)
                            .toList())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "개별 메뉴 상세 정보")
    public static class MenuDto {
        @Schema(description = "메뉴 ID", example = "10")
        private Long id;

        @Schema(description = "메뉴 이름", example = "해물파전")
        private String name;

        @Schema(description = "가격", example = "18000")
        private Integer price;

        @Schema(description = "메뉴 이미지 URL", example = "https://s3.../menu.jpg")
        private String imageUrl;

        public static MenuDto from(Menu entity) {
            return MenuDto.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .price(entity.getPrice())
                    .imageUrl(entity.getImageUrl())
                    .build();
        }
    }
}
