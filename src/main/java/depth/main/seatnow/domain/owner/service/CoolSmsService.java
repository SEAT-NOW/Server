package depth.main.seatnow.domain.owner.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoolSmsService {
    @Value("${coolsms.sms.accessKey}")
    private String ACCESS_KEY;  // CoolSMS API 접근 키

    @Value("${coolsms.sms.secretKey}")
    private String SECRET_KEY;  // CoolSMS API 비밀 키

    @Value("${coolsms.sms.senderPhoneNumber}")
    private String SENDER_PHONE_NUMBER;  // 발신자 번호

    private DefaultMessageService messageService;

    // CoolSMS 초기화
    @PostConstruct
    public void init() {
        // API 키 및 Secret 키를 이용해 CoolSMS API 객체를 초기화
        this.messageService = NurigoApp.INSTANCE.initialize(ACCESS_KEY, SECRET_KEY, "https://api.coolsms.co.kr");
    }

    // SMS 발송
    public void sendSms(String phoneNumber, String verificationCode) {
        // 인증 코드 메시지 생성
        Message message = new Message();
        message.setFrom(SENDER_PHONE_NUMBER); // 발신자 번호
        message.setTo(phoneNumber); // 수신자 번호
        message.setText("귀하의 인증 코드는 " + verificationCode + "입니다.");

        try {
            // CoolSMS API를 사용하여 메시지 발송
            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));

            // 응답 결과 확인
            if (response != null && response.getStatusCode() != null && response.getStatusCode().equals("200")) {
                log.info("SMS 전송 성공: " + response.getStatusMessage());
            } else {
                log.error("SMS 전송 실패: " + (response != null ? response.getStatusMessage() : "No response"));
            }
        } catch (Exception e) {
            log.error("SMS 전송 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
