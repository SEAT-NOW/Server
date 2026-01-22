package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "사장님 계정 정보 조회 응답")
public class AccountInfoResponse {
    @Schema(description = "사장님 휴대폰 번호", example = "01012341234")
    private String phoneNumber;

    @Schema(description = "사장님 이메일 주소", example = "owner@example.com")
    private String email;

    public static AccountInfoResponse of(User user) {
        return AccountInfoResponse.builder()
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .build();
    }
}
