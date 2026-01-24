package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.dto.request.update.MenuCategoryUpdateRequest;
import depth.main.seatnow.domain.store.dto.request.update.MenuUpdateRequest;
import depth.main.seatnow.domain.store.dto.response.StoreMenuResponse;
import depth.main.seatnow.domain.store.service.StoreMenuService;
import depth.main.seatnow.global.common.ApiResponse;
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

@Tag(name = "사장님 - 메뉴 관리", description = "메뉴 카테고리 설정 및 개별 메뉴 등록/수정/조회 API")
@RestController
@RequestMapping("/api/v1/stores/menus")
@RequiredArgsConstructor
public class StoreMenuController {
    private final StoreMenuService storeMenuService;

    @Operation(
            summary = "메뉴 카테고리 일괄 편집 [인증 필요]",
            description = "04-2-1 메뉴 카테고리 편집에서 기존 카테고리 수정/삭제 및 신규 카테고리를 추가합니다.\n\n" +
                    "**주의**: 카테고리 삭제 시 해당 카테고리에 속한 메뉴도 모두 삭제됩니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"success\": true, \"data\": true, \"message\": \"메뉴 카테고리가 성공적으로 수정되었습니다.\"}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "매장 또는 카테고리를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "매장 없음",
                                            summary = "사장님의 매장 정보를 찾을 수 없는 경우",
                                            value = "{\"code\": \"4041\", \"message\": \"존재하지 않는 매장입니다.\", \"detail\": null}"),
                                    @ExampleObject(
                                            name = "카테고리 없음",
                                            summary = "수정하려는 카테고리 ID가 해당 매장에 존재하지 않는 경우",
                                            value = "{\"code\": \"4046\", \"message\": \"존재하지 않는 메뉴 카테고리입니다.\", \"detail\": null}")
                            }
                    )
            )
    })
    @PatchMapping("/categories")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<Boolean> updateMenuCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid MenuCategoryUpdateRequest request
    ) {
        storeMenuService.updateMenuCategories(userDetails.getUserId(), request);
        return ApiResponse.ok(true, "메뉴 카테고리가 성공적으로 수정되었습니다.");
    }

    @Operation(
            summary = "메뉴 등록 및 상세 수정 [인증 필요]",
            description = "04-2-2메뉴 상세 편집에서 새로운 메뉴를 등록하거나 기존 메뉴 정보를 수정합니다.\n\n" +
                    "- **신규 등록**: id를 null로 보냅니다.\n" +
                    "- **정보 수정**: 해당 메뉴의 id를 포함하여 보냅니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )

    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"success\": true, \"data\": true, \"message\": \"메뉴 정보가 성공적으로 반영되었습니다.\"}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "대상 메뉴 또는 카테고리를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "카테고리 없음",
                                            summary = "카테고리 ID가 잘못된 경우",
                                            value = "{\"code\": \"4046\", \"message\": \"존재하지 않는 메뉴 카테고리입니다.\", \"detail\": null}"),
                                    @ExampleObject(
                                            name = "메뉴 없음",
                                            summary = "수정하려는 메뉴 ID가 잘못된 경우",
                                            value = "{\"code\": \"4047\", \"message\": \"존재하지 않는 메뉴입니다.\", \"detail\": null}")
                            }
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<Boolean> saveOrUpdateMenu(
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @RequestPart("menuData")
            @Parameter(
                    description = "메뉴 등록 및 수정 데이터 (JSON)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MenuUpdateRequest.class))
            )@Valid MenuUpdateRequest request,

            @RequestPart(value = "menuImage", required = false)
            @Parameter(description = "메뉴 이미지 파일 (이미지 변경 시에만 전송)")
            MultipartFile menuImage
    ) {
        storeMenuService.saveOrUpdateMenu(userDetails.getUserId(), request, menuImage);
        return ApiResponse.ok(true, "메뉴 정보가 성공적으로 반영되었습니다.");
    }

    @Operation(
            summary = "매장 메뉴 정보 조회 [인증 필요]",
            description = "04 가게 관리_상세페이지 관리 화면에서 카테고리별 메뉴 정보를 조회합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StoreMenuResponse.class)
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
    public ApiResponse<StoreMenuResponse> getStoreMenus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StoreMenuResponse response = storeMenuService.getStoreMenus(userDetails.getUserId());
        return ApiResponse.ok(response, "매장 메뉴 정보를 성공적으로 조회하였습니다.");
    }
}
