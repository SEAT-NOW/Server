package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.dto.request.OwnerSignupRequest;
import depth.main.seatnow.domain.store.service.StoreService;
import depth.main.seatnow.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    @PostMapping(value ="/owner/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> signup(
            @RequestPart("signupData") @Valid OwnerSignupRequest request,
            @RequestPart("licenseImage") MultipartFile licenseImage,
            @RequestPart("storeImages") List<MultipartFile> storeImages
    ) {
        storeService.registerOwner(request, licenseImage, storeImages);
        return ApiResponse.ok("사장님 회원가입 및 매장 등록이 완료되었습니다.");
    }
}

