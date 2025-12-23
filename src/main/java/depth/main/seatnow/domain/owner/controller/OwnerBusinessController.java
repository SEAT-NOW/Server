package depth.main.seatnow.domain.owner.controller;

import depth.main.seatnow.domain.owner.dto.request.VerifyBusinessNumberRequest;
import depth.main.seatnow.domain.owner.service.OwnerBusinessService;
import depth.main.seatnow.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/owner/business")
@RequiredArgsConstructor
public class OwnerBusinessController {
    private final OwnerBusinessService ownerBusinessService;

    @PostMapping("/verify")
    public ApiResponse<Boolean> verify(@RequestBody VerifyBusinessNumberRequest request) {
        boolean result = ownerBusinessService.verifyBusinessNumber(request.getBusinessNumber());
        return ApiResponse.ok(result, "사업자 등록번호가 유효하게 확인되었습니다.");
    }
}
