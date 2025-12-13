package depth.main.seatnow.global.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST("4000", "잘못된 요청입니다."),
    INVALID_BUSINESS_NUMBER("4001", "유효하지 않은 사업자번호입니다."),
    UNAUTHORIZED("4010", "인증이 필요합니다."),
    FORBIDDEN("4030", "접근 권한이 없습니다."),
    NOT_FOUND("4040", "리소스를 찾을 수 없습니다."),
    CONFLICT("4090", "이미 존재하는 리소스입니다."),
    SERVER_ERROR("5000", "서버 내부 오류입니다.");

    private final String code;
    private final String message;
}
