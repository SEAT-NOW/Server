package depth.main.seatnow.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "사업자 등록번호 확인 요청 객체")
public class VerifyBusinessNumberRequest {
    @Schema(description = "사업자 등록번호", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private String businessNumber;
}
