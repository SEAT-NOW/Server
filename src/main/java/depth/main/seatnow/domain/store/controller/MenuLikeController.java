package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.service.MenuLikeService;
import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuLikeController {

    private final MenuLikeService menuLikeService;

    @PostMapping("/{menuId}/like")
    public ApiResponse<String> toggleLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long menuId) {
        menuLikeService.toggleMenuLike(userDetails.getUserId(), menuId);
        return ApiResponse.ok("좋아요 상태가 변경되었습니다.");
    }
}
