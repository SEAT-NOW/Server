package depth.main.seatnow.domain.owner.service;

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
    private final CoolSmsService coolSmsService;

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
    public boolean verifyCode(String phoneNumber, String code) {
        String storedCode = redisTemplate.opsForValue().get("sms_verification:" + phoneNumber);
        return storedCode != null && storedCode.equals(code);
    }
}
