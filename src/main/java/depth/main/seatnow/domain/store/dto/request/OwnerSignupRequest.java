package depth.main.seatnow.domain.store.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OwnerSignupRequest {
    @Valid @NotNull private AccountRequest account;     // 계정 정보
    @Valid @NotNull private BusinessRequest business;   // 사업자 정보
    @Valid @NotEmpty private List<SpaceRequest> layout;  // 좌석 구성
    @Valid @NotNull private OperationRequest operation; // 운영 정보 (휴무 포함)
}
