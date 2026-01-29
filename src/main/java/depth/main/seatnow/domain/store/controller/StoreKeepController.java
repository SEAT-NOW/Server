package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.dto.response.KeptStoreListResponse;
import depth.main.seatnow.domain.store.service.StoreKeepService;
import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreKeepController {

    private final StoreKeepService storeKeepService;

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

    @GetMapping("/get-kept")
    public ApiResponse<List<KeptStoreListResponse>> getKeptStores(@AuthenticationPrincipal CustomUserDetails user) {
        List<KeptStoreListResponse> keptStores = storeKeepService.getKeptStores(user.getUserId());
        return ApiResponse.ok(keptStores);
    }
}
