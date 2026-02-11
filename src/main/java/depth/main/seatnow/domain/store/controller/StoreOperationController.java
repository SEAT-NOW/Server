package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.dto.request.update.OperationUpdateRequest;
import depth.main.seatnow.domain.store.dto.request.update.StorePhoneUpdateRequest;
import depth.main.seatnow.domain.store.dto.request.update.StorePhotoUpdateRequest;
import depth.main.seatnow.domain.store.dto.response.OperationInfoResponse;
import depth.main.seatnow.domain.store.dto.response.StorePhotoResponse;
import depth.main.seatnow.domain.store.service.StoreOperationService;
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

@Tag(name = "사장님 - 매장 운영 관리", description = "가게 연락처, 영업시간, 사진 관리 API")
@RestController
@RequestMapping("/api/v1/stores/operation")
@RequiredArgsConstructor
public class StoreOperationController {
    private final StoreOperationService storeOperationService;
    @Operation(
            summary = "가게 연락처 수정 [인증 필요]",
            description = "가게의 대표 연락처를 수정합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"data\": true, \"message\": \"가게 연락처가 성공적으로 수정되었습니다.\"}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 데이터 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "입력값 누락",
                                    value = "{\"code\": \"4000\", \"message\": \"잘못된 요청입니다.\", \"detail\": \"매장 전화번호를 입력해주세요.\"}"
                            ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "가게 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "가게 없음",
                                    summary = "STORE_NOT_FOUND",
                                    value = "{\"code\": \"4041\", \"message\": \"존재하지 않는 매장입니다.\", \"detail\": null}"
                            ))
            )
    })
    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/phone-number")
    public ApiResponse<Boolean> updateStorePhone(
            @Valid @RequestBody StorePhoneUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        storeOperationService.updateStorePhone(userDetails.getUserId(), request.getStorePhone());
        return ApiResponse.ok(true, "가게 연락처가 성공적으로 수정되었습니다.");
    }

    @Operation(
            summary = "매장 운영 정보 수정 [인증 필요]",
            description = "Bearer 토큰 인증이 필요하며, 04-1가게관리_영업정보 관리에서 사장님의 영업 시간, 정기 휴무, 임시 휴무 정보를 일괄 수정합니다." +
                    "ID가 포함된 항목은 수정, ID가 없는 항목은 신규 추가, 기존 리스트에서 누락된 항목은 삭제 처리됩니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "운영 정보 수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"data\": true, \"message\": \"매장 운영 정보가 성공적으로 수정되었습니다.\"}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 리소스",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "매장 없음",
                                            summary = "STORE_NOT_FOUND",
                                            value = "{\"code\": \"4041\", \"message\": \"존재하지 않는 매장입니다.\", \"detail\": null}"
                                    ),
                                    @ExampleObject(
                                            name = "영업시간 정보 없음",
                                            summary = "OPENING_HOUR_NOT_FOUND",
                                            value = "{\"code\": \"4044\", \"message\": \"존재하지 않는 영업시간입니다.\", \"detail\": null}"
                                    ),
                                    @ExampleObject(
                                            name = "휴무 정보 없음",
                                            summary = "HOLIDAY_NOT_FOUND",
                                            value = "{\"code\": \"4045\", \"message\": \"존재하지 않는 임시휴무입니다.\", \"detail\": null}"
                                    )
                            }
                    )
            )
    })
    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping
    public ApiResponse<Boolean> updateOperationInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid OperationUpdateRequest request) {

        storeOperationService.updateOperationInfo(userDetails.getUserId(), request);
        return ApiResponse.ok(true, "매장 운영 정보가 성공적으로 수정되었습니다.");
    }

    @Operation(
            summary = "매장 사진 일괄 수정 [인증 필요]",
            description = "04-3 가게 관리_매장 사진 관리에서 매장의 새로운 사진을 추가 업로드합니다.\n\n" +
                    "**[데이터 송신 규칙]**\n" +
                    "- **updateData**: 유지할 기존 사진의 ID와 대표 여부 정보 (JSON)\n" +
                    "- **newImages**: 새롭게 업로드할 이미지 파일 리스트 (File)\n\n" +
                    "**[비즈니스 로직 - 대표 사진 선정 우선순위]**\n" +
                    "1. 'updateData' 내 기존 사진 중 `isMain: true`인 항목이 있다면 해당 사진을 대표로 유지합니다.\n" +
                    "2. 'updateData'에 'isMain: true'가 없다면, `newImages` 리스트의 **첫 번째(0번 인덱스)** 사진을 자동으로 대표로 지정합니다.\n",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "매장 사진 수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"data\": true, \"message\": \"매장 사진이 성공적으로 수정되었습니다.\"}"))
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
    @PatchMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<Boolean> updateStoreImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @RequestPart("updateData", required = false)
            @Parameter(description = "기존 이미지 관리 정보 (JSON)", required = false)
            @Valid StorePhotoUpdateRequest request,

            @RequestPart(value = "newImages", required = false)
            @Parameter(description = "새로 추가할 매장 이미지 파일들")
            List<MultipartFile> newImages
    ) {
        storeOperationService.updateStoreImages(userDetails.getUserId(), request, newImages);
        return ApiResponse.ok(true, "매장 사진이 성공적으로 수정되었습니다.");
    }

    @Operation(
            summary = "매장 운영 정보 조회 [인증 필요]",
            description = "04-1 가게관리_영업정보 관리 화면에 필요한 모든 운영 정보를 조회합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OperationInfoResponse.class)
                    )
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
    @GetMapping
    public ApiResponse<OperationInfoResponse> getOperationInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        OperationInfoResponse response = storeOperationService.getOperationInfo(userDetails.getUserId());
        return ApiResponse.ok(response, "매장 운영 정보를 성공적으로 조회하였습니다.");
    }

    @Operation(
            summary = "매장 사진 조회 [인증 필요]",
            description = "04-3 가게 관리_매장 사진 관리 화면에서 현재 등록된 사진 리스트를 조회합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StorePhotoResponse.class)
                    )
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
    @GetMapping("/images")
    public ApiResponse<StorePhotoResponse> getStoreImages(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StorePhotoResponse response = storeOperationService.getStoreImages(userDetails.getUserId());
        return ApiResponse.ok(response, "매장 사진 정보를 성공적으로 조회하였습니다.");
    }
}
