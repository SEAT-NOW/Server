package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.dto.request.OwnerSignupRequest;
import depth.main.seatnow.domain.store.service.StoreService;
import depth.main.seatnow.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    @PostMapping("/owner/signup")
    public ApiResponse<String> signup(@RequestBody OwnerSignupRequest request) {
        storeService.registerOwner(request);
        return ApiResponse.ok("사장님 회원가입 및 매장 등록이 완료되었습니다.");
    }
}

