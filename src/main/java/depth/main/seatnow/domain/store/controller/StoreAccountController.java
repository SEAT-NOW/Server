package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.dto.request.OwnerWithdrawRequest;
import depth.main.seatnow.domain.store.dto.request.signup.OwnerSignupRequest;
import depth.main.seatnow.domain.store.dto.response.StoreProfileResponse;
import depth.main.seatnow.domain.store.service.StoreAccountService;
import depth.main.seatnow.domain.user.dto.request.VerifyPasswordRequest;
import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.global.exception.error.ErrorResponse;
import depth.main.seatnow.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "사장님 - 계정 관리", description = "사장님 회원가입, 탈퇴 및 비밀번호 관리 API")
@RestController
@RequestMapping("/api/v1/stores/owner")
@RequiredArgsConstructor
public class StoreAccountController {
    private final StoreAccountService storeAccountService;
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
    @PostMapping(value ="/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
        Long storeId =  storeAccountService.registerOwner(request, licenseImage, storeImages);
        return ApiResponse.ok(
                Map.of("storeId", storeId),
                "사장님 회원가입 및 매장 등록이 완료되었습니다."
        );
    }

    @Operation(
            summary = "사장님 회원탈퇴 [인증 필요]",
            description = "Bearer 토큰 인증이 필요하며, 사업자번호와 비밀번호를 확인하여 매장 및 사장님 계정을 삭제합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원탈퇴 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"success\": true, \"data\": null, \"message\": \"사장님 회원탈퇴가 완료되었습니다.\"}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "검증 실패 (비밀번호 또는 사업자번호 불일치)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "비밀번호 불일치",
                                            summary = "PASSWORD_MISMATCH",
                                            value = "{\"code\": \"4004\", \"message\": \"유효하지 않은 비밀번호입니다.\", \"detail\": null}"
                                    ),
                                    @ExampleObject(
                                            name = "사업자 번호 불일치",
                                            summary = "INVALID_BUSINESS_NUMBER",
                                            value = "{\"code\": \"4001\", \"message\": \"유효하지 않은 사업자번호입니다.\", \"detail\": null}"
                                    )
                            }
                    )
            )
    })
    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping
    public ApiResponse<Void> withdrawOwner(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OwnerWithdrawRequest request
    ) {
        storeAccountService.withdrawOwner(userDetails, request);
        return ApiResponse.ok(null, "사장님 회원탈퇴가 완료되었습니다.");
    }

    @Operation(
            summary = "비밀번호 확인 [인증 필요]",
            description = "03-1-1마이페이지_계정정보 수정에서 현재 비밀번호가 일치하는지 확인합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }

    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "확인 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"data\": true, \"message\": \"비밀번호 확인에 성공하였습니다.\"}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\": \"4005\", \"message\": \"유효하지 않은 비밀번호입니다.\", \"detail\": null}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "유저 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\": \"4040\", \"message\": \"존재하지 않는 사용자입니다.\", \"detail\": null}"))
            )
    })
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/verify-password")
    public ApiResponse<Boolean> verifyPassword(
            @Valid @RequestBody VerifyPasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        storeAccountService.verifyPassword(userDetails.getUserId(), request.getPassword());
        return ApiResponse.ok(true, "비밀번호 확인에 성공하였습니다.");
    }

    @Operation(
            summary = "비밀번호 변경 [인증 필요]",
            description = "로그인된 사용자의 비밀번호를 새 비밀번호로 변경합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"data\": true, \"message\": \"비밀번호가 성공적으로 수정되었습니다.\"}"))
            )
    })
    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/password")
    public ApiResponse<Boolean> updatePassword(
            @Valid @RequestBody VerifyPasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        storeAccountService.updatePassword(userDetails.getUserId(), request.getPassword());
        return ApiResponse.ok(true, "비밀번호가 성공적으로 수정되었습니다.");
    }

    @Operation(
            summary = "가게 기초 정보 조회 [인증 필요]",
            description = "03-2 마이페이지_가게 정보 수정 화면에 필요한 사업자 및 기초 정보를 조회합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = StoreProfileResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "매장 정보를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "매장 없음",
                                    value = "{\"code\": \"4041\", \"message\": \"존재하지 않는 매장입니다.\", \"detail\": null}"
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/profile")
    public ApiResponse<StoreProfileResponse> getStoreProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StoreProfileResponse response = storeAccountService.getStoreProfile(userDetails.getUserId());
        return ApiResponse.ok(response, "가게 기초 정보를 성공적으로 조회하였습니다.");
    }
}
