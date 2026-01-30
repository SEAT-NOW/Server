package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.dto.response.KeptStoreListResponse;
import depth.main.seatnow.domain.store.service.StoreKeepService;
import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Tag(name = "킵한 매장 관리", description = "매장 킵하기, 조회하기")
public class StoreKeepController {

    private final StoreKeepService storeKeepService;

    @Operation(summary = "매장 킵하기", description = "즐겨찾기가 된 매장이면 취소, 안된 매장이면 등록합니다.")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{storeId}/keep")
    public ApiResponse<Boolean> keepStore(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long storeId
    ) {
        boolean isKept = storeKeepService.keepStore(user.getUserId(), storeId);

        if (isKept) {
            return ApiResponse.ok(isKept, "즐겨찾기가 등록되었습니다.");
        } else {
            return ApiResponse.ok(isKept, "즐겨찾기가 취소되었습니다.");
        }
    }

    @Operation(summary = "킵한 매장 조회하기", description = "유저가 킵한 매장을 모두 조회합니다.")
    @GetMapping("/kept")
    public ApiResponse<List<KeptStoreListResponse>> getKeptStores(@AuthenticationPrincipal CustomUserDetails user) {
        List<KeptStoreListResponse> keptStores = storeKeepService.getKeptStores(user.getUserId());
        return ApiResponse.ok(keptStores);
    }
}
