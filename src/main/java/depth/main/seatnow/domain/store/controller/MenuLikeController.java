package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.service.MenuLikeService;
import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
@Tag(name = "메뉴 좋아요 기능", description = "메뉴 좋아요 누르기, 취소하기")
public class MenuLikeController {

    private final MenuLikeService menuLikeService;

    @Operation(summary = "메뉴 좋아요 등록/취소", description = "특정 메뉴에 좋아요를 누르거나 취소하기.")
    @PostMapping("/{menuId}/like")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Boolean> toggleLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "좋아요를 누를 메뉴의 ID", required = true, example = "3")
            @PathVariable Long menuId) {
        boolean isLiked = menuLikeService.toggleMenuLike(userDetails.getUserId(), menuId);

        if (isLiked) {
            return ApiResponse.ok(isLiked, "좋아요가 눌렸습니다.");
        } else {
            return ApiResponse.ok(isLiked, "좋아요가 취소되었습니다.");
        }
    }
}
