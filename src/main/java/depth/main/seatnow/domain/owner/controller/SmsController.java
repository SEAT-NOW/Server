package depth.main.seatnow.domain.owner.controller;

import depth.main.seatnow.domain.owner.service.SmsVerificationService;
import depth.main.seatnow.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class SmsController {
    private final SmsVerificationService smsVerificationService;

    // SMS 인증 코드 발송 요청
    @PostMapping("/send-sms-verification-code")
    public ApiResponse<String> sendSmsVerificationCode(@RequestParam String phoneNumber) {
        smsVerificationService.sendVerificationCode(phoneNumber); // 인증 코드 발송
        return ApiResponse.ok("인증 코드가 SMS로 발송되었습니다.");
    }

    // SMS 인증 코드 확인 요청
    @PostMapping("/verify-sms-code")
    public ApiResponse<Boolean> verifySmsCode(@RequestParam String phoneNumber, @RequestParam String code) {
        boolean isValid = smsVerificationService.verifyCode(phoneNumber, code); // 인증 코드 검증
        return ApiResponse.ok(isValid, isValid ? "인증 성공" : "인증 실패 (유효하지 않거나 만료된 코드)");
    }
}
