package depth.main.seatnow.domain.store.dto.request.signup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Schema(description = "사장님 계정 정보")
@Getter
@NoArgsConstructor
public class AccountRequest {
    @Schema(description = "로그인용 이메일", example = "owner@seatnow.com")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "비밀번호 (8자 이상, 영문/숫자/특수문자 조합)", example = "Password123!")
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\W).{8,}$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\W).{8,}$",
            message = "비밀번호는 8자 이상이며, 영문 대소문자/숫자/특수문자 중 3종류 이상을 조합해야 합니다."
    ) // 8자 이상 + 3종류 조합 정규식
    private String password;

    @Schema(description = "사장님 개인 전화번호", example = "01012345678")
    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    private String phoneNumber;
}
