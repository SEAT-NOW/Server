package depth.main.seatnow.global.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "4000", "잘못된 요청입니다."),
    INVALID_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST, "4001", "유효하지 않은 사업자번호입니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "4002", "인증 번호가 일치하지 않습니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "4003", "인증 시간이 만료되었습니다. 다시 시도해주세요."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"4010", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"4011", "잘못된 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,"4012", "토큰이 만료되었습니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"4013", "리프레시 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"4014", "리프레시 토큰이 유효하지 않습니다"),

    FORBIDDEN(HttpStatus.FORBIDDEN,"4030", "접근 권한이 없습니다."),

    NOT_FOUND(HttpStatus.NOT_FOUND,"4040", "존재하지 않는 사용자입니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND,"4041", "존재하지 않는 매장입니다."),
    TABLE_NOT_FOUND(HttpStatus.NOT_FOUND,"4042", "매장에 존재하지 않는 테이블입니다."),
    SPACE_NOT_FOUND(HttpStatus.NOT_FOUND,"4043", "매장에 존재하지 않는 공간입니다."),

    CONFLICT(HttpStatus.CONFLICT, "4090", "이미 존재하는 리소스입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "4091", "이미 가입된 이메일입니다."),
    DUPLICATE_BUSINESS_NUMBER(HttpStatus.CONFLICT, "4092", "이미 등록된 사업자 번호입니다."),

    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"5000", "서버 내부 오류입니다."),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"5001", "외부 시스템과의 통신 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
