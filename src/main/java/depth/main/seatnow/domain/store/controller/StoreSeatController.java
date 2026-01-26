package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.dto.request.update.SpaceSeatUpdateRequest;
import depth.main.seatnow.domain.store.dto.response.SeatResponse;
import depth.main.seatnow.domain.store.dto.response.SpaceSeatUpdateResponse;
import depth.main.seatnow.domain.store.service.SeatService;
import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.global.exception.error.ErrorResponse;
import depth.main.seatnow.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "사장님 - 실시간 좌석 관리", description = "매장 좌석 현황 조회/업데이트 API")
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreSeatController {
    private final SeatService seatService;
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

        SeatResponse response = seatService.getStoreSeatStatus(storeId, userDetails);
        return ApiResponse.ok(response);
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
}
