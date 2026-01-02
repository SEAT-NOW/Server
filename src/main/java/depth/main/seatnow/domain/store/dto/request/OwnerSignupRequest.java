package depth.main.seatnow.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Schema(description = "사장님 회원가입 통합 요청")
@Getter
@NoArgsConstructor
public class OwnerSignupRequest {
    @Schema(description = "계정 정보")
    @Valid @NotNull private AccountRequest account;     // 계정 정보

    @Schema(description = "매장 사업자 정보")
    @Valid @NotNull private BusinessRequest business;   // 사업자 정보

    @Schema(description = "층별/구역별 좌석 레이아웃 리스트")
    @Valid @NotEmpty private List<SpaceRequest> layout;  // 좌석 구성

    @Schema(description = "정기/임시 휴무 및 영업시간 정보")
    @Valid @NotNull private OperationRequest operation; // 운영 정보 (휴무 포함)
}
