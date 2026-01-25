package depth.main.seatnow.domain.auth.service;

import depth.main.seatnow.domain.user.repository.UserRepository;
import depth.main.seatnow.global.exception.custom.BadRequestException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${email.verification.expiry-time}")
    private long expiryTimeInMinutes;

    // 이메일 인증 코드 발송
    public void sendVerificationCode(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException(ErrorCode.DUPLICATE_EMAIL);
        }

        String verificationCode = generateVerificationCode();

        // Redis에 인증 코드 저장
        redisTemplate.opsForValue().set("email_verification:" + email, verificationCode, expiryTimeInMinutes, TimeUnit.MINUTES);

        sendEmail(email, verificationCode);
    }

    // 이메일 인증 코드 확인
    public void verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get("email_verification:" + email);
        // 1. 만료되었거나 코드가 존재하지 않는 경우
        if (storedCode == null) {
            throw new BadRequestException(ErrorCode.EXPIRED_VERIFICATION_CODE);
        }

        // 2. 코드가 일치하지 않는 경우
        if (!storedCode.equals(code)) {
            throw new BadRequestException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 인증 성공 시, 번호는 지우고 '이메일 인증 완료 증표'를 저장
        redisTemplate.delete("email_verification:" + email);
        redisTemplate.opsForValue().set("email_completed:" + email, "true", expiryTimeInMinutes, TimeUnit.MINUTES);
    }

    // 인증 코드 생성
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);  // 6자리 랜덤 코드 생성
        return String.valueOf(code);
    }

    // 이메일로 인증 코드 보내기
    private void sendEmail(String email, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("SeatNow 인증번호");
        message.setText("[SeatNow] 인증번호는 [" + verificationCode + "] 입니다. 3분 이내에 입력해주세요.");
        mailSender.send(message);
    }


}
