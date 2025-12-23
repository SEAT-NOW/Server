package depth.main.seatnow.domain.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class EmailVerificationService {
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${email.verification.expiry-time}")
    private long expiryTimeInMinutes;

    public EmailVerificationService(StringRedisTemplate redisTemplate, JavaMailSender mailSender) {
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
    }

    // 인증 코드 생성
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);  // 6자리 랜덤 코드 생성
        return String.valueOf(code);
    }

    // 이메일 인증 코드 발송
    public void sendVerificationCode(String email) {
        String verificationCode = generateVerificationCode();

        // Redis에 인증 코드 저장
        redisTemplate.opsForValue().set("email_verification:" + email, verificationCode, expiryTimeInMinutes, TimeUnit.MINUTES);

        sendEmail(email, verificationCode);
    }

    // 이메일로 인증 코드 보내기
    private void sendEmail(String email, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("이메일 인증 코드");
        message.setText("인증 코드: " + verificationCode);
        mailSender.send(message);
    }

    // 이메일 인증 코드 확인
    public boolean verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get("email_verification:" + email);
        return storedCode != null && storedCode.equals(code);
    }
}
