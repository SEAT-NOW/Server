package depth.main.seatnow.global.exception.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "에러 발생 시 반환되는 공통 에러 응답 객체")
public class ErrorResponse {
    @Schema(description = "비즈니스 에러 코드", example = "4001")
    private String code;

    @Schema(description = "에러 메시지")
    private String message;

    @Schema(description = "에러 상세 원인 (개발자 디버깅 및 프론트엔드 상세 처리용)")
    private String detail;
}
