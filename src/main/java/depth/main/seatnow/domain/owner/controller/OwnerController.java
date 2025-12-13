package depth.main.seatnow.domain.owner.controller;

import depth.main.seatnow.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import depth.main.seatnow.domain.owner.service.EmailVerificationService;
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerController {
    private final EmailVerificationService emailVerificationService;
    // 이메일 인증 코드 발송
    @PostMapping("/send-verification-code")
    public ApiResponse<String> sendVerificationCode(@RequestParam String email) {
        emailVerificationService.sendVerificationCode(email);
        return ApiResponse.ok("인증 코드가 이메일로 발송되었습니다.");
    }

    // 이메일 인증 코드 확인
    @PostMapping("/verify-code")
    public ApiResponse<Boolean> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean isValid = emailVerificationService.verifyCode(email, code);
        return ApiResponse.ok(isValid, isValid ? "인증 성공" : "인증 실패");
    }
}
