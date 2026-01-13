package depth.main.seatnow.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "사장님 회원탈퇴 요청")
public class OwnerWithdrawRequest {
    @Schema(description = "사업자 등록번호", example = "123-45-67890")
    @NotBlank(message = "사업자 등록번호는 필수입니다.")
    private String businessNumber;

    @Schema(description = "비밀번호", example = "password1234")
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
