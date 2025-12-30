package depth.main.seatnow.domain.store.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountRequest {
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\W).{8,}$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\W).{8,}$",
            message = "비밀번호는 8자 이상이며, 영문 대소문자/숫자/특수문자 중 3종류 이상을 조합해야 합니다."
    ) // 8자 이상 + 3종류 조합 정규식
    private String password;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    private String phoneNumber;
}
