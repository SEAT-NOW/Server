package depth.main.seatnow.global.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST("4000", "잘못된 요청입니다."),
    INVALID_BUSINESS_NUMBER("4001", "유효하지 않은 사업자번호입니다."),
    INVALID_VERIFICATION_CODE("4002", "인증 번호가 일치하지 않습니다."),
    EXPIRED_VERIFICATION_CODE("4003", "인증 시간이 만료되었습니다. 다시 시도해주세요."),

    UNAUTHORIZED("4010", "인증이 필요합니다."),
    INVALID_TOKEN("4011", "잘못된 토큰입니다."),
    EXPIRED_TOKEN("4012", "토큰이 만료되었습니다."),
    EXPIRED_REFRESH_TOKEN("4013", "리프레시 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN("4014", "리프레시 토큰이 유효하지 않습니다"),

    FORBIDDEN("4030", "접근 권한이 없습니다."),

    NOT_FOUND("4040", "리소스를 찾을 수 없습니다."),

    CONFLICT("4090", "이미 존재하는 리소스입니다."),

    SERVER_ERROR("5000", "서버 내부 오류입니다."),
    EXTERNAL_API_ERROR("5001", "외부 시스템과의 통신 중 오류가 발생했습니다.");



    private final String code;
    private final String message;
}
