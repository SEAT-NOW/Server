package depth.main.seatnow.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Schema(description = "사장님 로그인 요청 객체")
@Getter
@NoArgsConstructor
public class OwnerLoginRequest {
    @Schema(description = "사장님 계정 이메일", example = "owner@seatnow.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "사장님 계정 비밀번호", example = "password123!")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
