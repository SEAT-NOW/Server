package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.dto.request.OwnerSignupRequest;
import depth.main.seatnow.domain.store.dto.request.SpaceSeatUpdateRequest;
import depth.main.seatnow.domain.store.dto.request.OwnerWithdrawRequest;
import depth.main.seatnow.domain.store.dto.response.SeatResponse;
import depth.main.seatnow.domain.store.dto.response.SpaceSeatUpdateResponse;
import depth.main.seatnow.domain.store.service.SeatService;
import depth.main.seatnow.domain.store.dto.response.StoreListResponse;
import depth.main.seatnow.domain.store.service.StoreService;
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

@Tag(name = "매장/사장님 관리", description = "사장님 회원가입 및 매장 설정 관련 API")
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;
    private final SeatService seatService;
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
    @Operation(
            summary = "실시간 좌석 현황 조회 [인증 필요]",
            description = "Bearer 토큰 인증이 필요하며, 매장의 공간별/테이블별 이용 및 빈 좌석 현황을 조회합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "본인 매장 아님",
                                            summary = "FORBIDDEN",
                                            value = "{\"code\": \"4030\", \"message\": \"접근 권한이 없습니다.\", \"detail\": null}"
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "매장 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "매장 존재하지 않음",
                                    summary = "NOT_FOUND",
                                    value = "{\"code\": \"4040\", \"message\": \"존재하지 않는 매장입니다.\", \"detail\": null}"
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/{storeId}/seats")
    public ApiResponse<SeatResponse> getStoreSeats(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SeatResponse response = storeService.getStoreSeatStatus(storeId, userDetails);
        return ApiResponse.ok(response);
    }

    @Operation(
            summary = "술집 검색 및 지도 조회",
            description = "홈 화면, N명 자리 찾기, 키워드 검색 API" +
                    "headCount가 1이상이면 좌석 수로 필터링, 결과는 최신 업데이트 순으로 정렬"
    )
    @GetMapping("/search")
    public ApiResponse<List<StoreListResponse>> searchStores(
            @Parameter(description = "검색어 (가게명 or 주소)")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "위도")
            @RequestParam(required = false) Double lat,

            @Parameter(description = "경도")
            @RequestParam(required = false) Double lng,

            @Parameter(description = "검색 반경 (km 단위, 기본값 1.0)")
            @RequestParam(defaultValue = "1.0") Double radius,

            @Parameter(description = "인원수 필터 (0: 전체, N: N석 이상 남은 곳")
            @RequestParam(defaultValue = "0") Integer headCount) {
        List<StoreListResponse> response = storeService.searchStores(keyword, lat, lng, radius, headCount);
        return ApiResponse.ok(response, "조회에 성공하였습니다.");
    }

    @Operation(
            summary = "실시간 좌석 현황 업데이트 [인증 필요]",
            description = "Bearer 토큰 인증이 필요하며, 사장님이 매장의 좌석 이용 현황을 직접 수정합니다. " +
                    "**[주의] 빈 좌석(Empty Seat)을 기준으로 업데이트하더라도, 반드시 '이용 중인 좌석 수(usedCount)'로 계산하여 전송해야 합니다.** " +
                    "모든 좌석 계산의 기준점은 '이용 좌석'이며, 빈 좌석 정보는 서버에서 전체 좌석과의 차이로 자동 산출됩니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업데이트 성공",
                    content = @Content(schema = @Schema(implementation = SpaceSeatUpdateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (음수 입력 또는 전체 좌석 수 초과)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "좌석 수 초과",
                                            summary = "INVALID_TABLE_COUNT",
                                            value = "{\"code\": \"4004\", \"message\": \"사용 중인 테이블 수는 전체 테이블 수를 초과할 수 없습니다.\", \"detail\": null}"
                                    ),
                                    @ExampleObject(
                                            name = "음수 입력",
                                            summary = "VALIDATION_ERROR",
                                            value = "{\"code\": \"4000\", \"message\": \"잘못된 요청입니다.\", \"detail\": \"테이블 수는 0개 이상이어야 합니다.\"}"
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (본인 매장이 아니거나 OWNER 권한 없음)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "접근 권한 없음",
                                            summary = "FORBIDDEN",
                                            value = "{\"code\": \"4030\", \"message\": \"접근 권한이 없습니다.\", \"detail\": null}"
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 리소스 (매장, 구역, 테이블 ID 오류)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(name = "매장 없음", value = "{\"code\": \"4041\", \"message\": \"존재하지 않는 매장입니다.\", \"detail\": null}"),
                                    @ExampleObject(name = "테이블 없음", value = "{\"code\": \"4042\", \"message\": \"존재하지 않는 테이블입니다.\", \"detail\": null}"),
                                    @ExampleObject(name = "공간 없음", value = "{\"code\": \"4043\", \"message\": \"존재하지 않는 공간입니다.\", \"detail\": null}")
                            }
                    )
            )
    })

    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/seats")
    public ApiResponse<SpaceSeatUpdateResponse> updateStoreSeats(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid SpaceSeatUpdateRequest request) {

        SpaceSeatUpdateResponse response = seatService.updateAllSeats(userDetails, request);
        return ApiResponse.ok(response, "좌석 현황이 성공적으로 업데이트되었습니다.");
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
    @DeleteMapping("/owner")
    public ApiResponse<Void> withdrawOwner(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OwnerWithdrawRequest request
    ) {
        storeService.withdrawOwner(userDetails, request);
        return ApiResponse.ok(null, "사장님 회원탈퇴가 완료되었습니다.");
    }
}

