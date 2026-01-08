package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.dto.request.OwnerSignupRequest;
import depth.main.seatnow.domain.store.dto.response.SeatResponse;
import depth.main.seatnow.domain.store.service.StoreService;
import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.global.exception.error.ErrorResponse;
import depth.main.seatnow.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "매장/사장님 관리", description = "사장님 회원가입 및 매장 설정 관련 API")
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;
    @Operation(
            summary = "사장님 회원가입 및 매장 등록",
            description = "계정 정보, 사업자 정보, 매장 레이아웃, 운영 시간을 한 번에 등록합니다. (Multipart Form Data 방식)"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = "{\"success\": true, \"data\": {\"storeId\": 1}, \"message\": \"사장님 회원가입 및 매장 등록이 완료되었습니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "중복 데이터 발생",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "이미 가입된 이메일",
                                            summary = "DUPLICATE_EMAIL",
                                            value = "{\"code\": \"4091\", \"message\": \"이미 가입된 이메일입니다.\", \"detail\": \"해당 이메일로 등록된 계정이 이미 존재합니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "이미 등록된 사업자 번호",
                                            summary = "DUPLICATE_BUSINESS_NUMBER",
                                            value = "{\"code\": \"4092\", \"message\": \"이미 등록된 사업자 번호입니다.\", \"detail\": \"해당 사업자 번호로 등록된 매장이 이미 존재합니다.\"}"
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 누락 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "필수값 누락",
                                    summary = "INVALID_REQUEST",
                                    value = "{\"code\": \"4000\", \"message\": \"잘못된 요청입니다.\", \"detail\": \"이메일은 필수 입력값입니다.\"}"
                            )
                    )
            )
    })
    @PostMapping(value ="/owner/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Long>> signup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OwnerSignupRequest.class)
                    )
            )
            @RequestPart("signupData") @Valid OwnerSignupRequest request,

            @Parameter(description = "사업자 등록증 이미지 파일")
            @RequestPart(value = "licenseImage", required = false) MultipartFile licenseImage,

            @Parameter(description = "매장 사진 리스트")
            @RequestPart(value = "storeImages", required = false) List<MultipartFile> storeImages
    ) {
        Long storeId =  storeService.registerOwner(request, licenseImage, storeImages);
        return ApiResponse.ok(
                    Map.of("storeId", storeId),
                "사장님 회원가입 및 매장 등록이 완료되었습니다."
        );
    }
    @Operation(summary = "실시간 좌석 현황 조회", description = "매장의 공간별/테이블별 이용 및 빈 좌석 현황을 조회합니다.")
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/{storeId}/seats")
    public ApiResponse<SeatResponse> getStoreSeats(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SeatResponse response = storeService.getStoreSeatStatus(storeId, userDetails);
        return ApiResponse.ok(response);
    }
}

