package depth.main.seatnow.global.exception.handler;

import depth.main.seatnow.global.exception.custom.CustomException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import depth.main.seatnow.global.exception.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1) CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        ErrorCode code = ex.getErrorCode();

        return ResponseEntity
                .status(getHttpStatus(code))
                .body(ErrorResponse.builder()
                        .code(code.getCode())
                        .message(code.getMessage())
                        .detail(ex.getDetail())
                        .build());
    }

    // 2) @Valid 에러 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .code("4000")
                        .message("잘못된 요청입니다.")
                        .detail(ex.getBindingResult().getFieldError().getDefaultMessage())
                        .build());
    }

    // 3) 알 수 없는 서버 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .code("5000")
                        .message("서버 내부 오류입니다.")
                        .detail(ex.getMessage())
                        .build());
    }

    private HttpStatus getHttpStatus(ErrorCode code) {
        return code.getHttpStatus();
    }
}
