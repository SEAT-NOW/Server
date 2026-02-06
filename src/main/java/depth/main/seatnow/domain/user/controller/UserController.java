package depth.main.seatnow.domain.user.controller;

import depth.main.seatnow.domain.user.dto.request.VerifyPasswordRequest;
import depth.main.seatnow.domain.user.dto.response.UserProfileResponse;
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

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileResponse response = userService.getMyProfile(userDetails.getUserId());
        return ApiResponse.ok(response);
    }

}
