package depth.main.seatnow.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SmsVerifyRequest {
    @Schema(description = "인증을 진행할 휴대폰 번호", example = "01012345678")
    @NotBlank(message = "휴대폰 번호는 필수 입력 항목입니다.")
    private String phoneNumber;

    @Schema(description = "수신한 6자리 인증 코드", example = "123456")
    @NotBlank(message = "인증 코드는 필수 입력 항목입니다.")
    @Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다.")
    private String code;
}
