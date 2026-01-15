package depth.main.seatnow.domain.auth.service;

import depth.main.seatnow.global.exception.custom.BadRequestException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SmsVerificationService {
    private final StringRedisTemplate redisTemplate;
    private final depth.main.seatnow.domain.auth.service.CoolSmsService coolSmsService;

    @Value("${coolsms.sms.verification.expiry-time}")
    private long expiryTimeInMinutes;  // 인증 코드 유효 시간 (분 단위)

    // 인증 코드 생성
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);  // 6자리 랜덤 코드 생성
        return String.valueOf(code);
    }

    // SMS 인증 코드 발송
    public void sendVerificationCode(String phoneNumber) {
        String verificationCode = generateVerificationCode();

        // Redis에 인증 코드 저장 (유효 시간 설정)
        redisTemplate.opsForValue().set("sms_verification:" + phoneNumber, verificationCode, expiryTimeInMinutes, TimeUnit.MINUTES);

        // CoolSMS를 이용하여 실제 SMS 발송
        coolSmsService.sendSms(phoneNumber, verificationCode);
    }

    // SMS 인증 코드 확인
    public void verifyCode(String phoneNumber, String code) {
        String storedCode = redisTemplate.opsForValue().get("sms_verification:" + phoneNumber);
        if (storedCode == null) {
            throw new BadRequestException(ErrorCode.EXPIRED_VERIFICATION_CODE);
        }

        if (!storedCode.equals(code)) {
            throw new BadRequestException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 인증 성공 시 삭제
        redisTemplate.delete("sms_verification:" + phoneNumber);

        // 인증 완료 증표를 저장
        redisTemplate.opsForValue().set("sms_completed:" + phoneNumber, "true", expiryTimeInMinutes, TimeUnit.MINUTES);
    }
}
