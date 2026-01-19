package depth.main.seatnow.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateStorePhoneRequest {
    @Schema(description = "매장 전화번호", example = "021234567")
    @NotBlank(message = "매장 전화번호를 입력해주세요.")
    private String storePhone;
}
