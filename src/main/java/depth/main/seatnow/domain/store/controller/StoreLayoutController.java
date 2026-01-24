package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.dto.request.update.SpaceUpdateRequest;
import depth.main.seatnow.domain.store.service.StoreLayoutService;
import depth.main.seatnow.global.common.ApiResponse;
import depth.main.seatnow.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "사장님 - 좌석 구조 설정", description = "매장의 공간(층/구역) 및 테이블 배치 조회/수정 API")
@RestController
@RequestMapping("/api/v1/stores/layout")
@RequiredArgsConstructor
public class StoreLayoutController {
    private final StoreLayoutService storeLayoutService;

    @Operation(
            summary = "매장 좌석 구성 정보 수정 [인증 필요]",
            description = "Bearer 토큰 인증이 필요하며, 03-3마이페이지_좌석 구성 정보 수정에서 매장의 공간(층/구역) 이름과 전체 테이블 구성을 일괄 수정합니다. " +
                    "ID가 포함된 항목은 수정, ID가 없는 항목은 신규 추가, 기존 리스트에서 누락된 항목은 삭제 처리됩니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "좌석 구성 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"success\": true, \"data\": true, \"message\": \"좌석 구성 정보가 성공적으로 수정되었습니다.\"}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "정보를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "공간 찾을 수 없음",
                                            summary = "SPACE_NOT_FOUND",
                                            value = "{\"code\": \"4043\", \"message\": \"매장에 존재하지 않는 공간입니다.\", \"detail\": null}"
                                    ),
                                    @ExampleObject(
                                            name = "테이블 찾을 수 없음",
                                            summary = "TABLE_NOT_FOUND",
                                            value = "{\"code\": \"4042\", \"message\": \"매장에 존재하지 않는 테이블입니다.\", \"detail\": null}"
                                    )
                            }
                    )
            )
    })
    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping
    public ApiResponse<Boolean> updateSpaceLayout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid List<SpaceUpdateRequest> request) {

        storeLayoutService.updateSpaces(userDetails.getUserId(), request);

        return ApiResponse.ok(true, "좌석 구성 정보가 성공적으로 수정되었습니다.");
    }
}
