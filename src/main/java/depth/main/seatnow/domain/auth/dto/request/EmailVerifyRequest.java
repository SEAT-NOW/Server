package depth.main.seatnow.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "이메일 인증 요청 객체")
public class EmailVerifyRequest {
    @Schema(description = "인증을 진행할 이메일 주소", example = "owner@example.com")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "이메일로 수신한 6자리 인증 코드", example = "123456")
    @NotBlank(message = "인증 코드는 필수 입력 항목입니다.")
    @Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다.")
    private String code;
}
