package depth.main.seatnow.domain.user.controller;

import depth.main.seatnow.domain.user.dto.request.VerifyPasswordRequest;
import depth.main.seatnow.domain.user.service.UserService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 관리", description = "일반 사용자 관련 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @Operation(
            summary = "일반 사용자 회원탈퇴 [인증 필요]",
            description = "Bearer 토큰 인증이 필요하며, 로그인한 사용자의 모든 데이터를 영구 삭제하고 탈퇴 처리합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원탈퇴 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"success\": true, \"data\": null, \"message\": \"회원탈퇴가 완료되었습니다.\"}")
                    )
            )
    })
    @DeleteMapping
    public ApiResponse<Void> withdrawUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.withdrawUser(userDetails);
        return ApiResponse.ok(null, "회원탈퇴가 완료되었습니다.");
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
        userService.verifyPassword(userDetails.getUserId(), request.getPassword());
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
        userService.updatePassword(userDetails.getUserId(), request.getPassword());
        return ApiResponse.ok(true, "비밀번호가 성공적으로 수정되었습니다.");
    }
}
