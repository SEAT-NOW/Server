package depth.main.seatnow.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
@Getter
@NoArgsConstructor
public class VerifyPasswordRequest {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
