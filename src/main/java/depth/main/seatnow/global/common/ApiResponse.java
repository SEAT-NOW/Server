package depth.main.seatnow.global.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "공통 응답 포맷")
public class ApiResponse <T>{
    @Schema(description = "성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "실제 데이터 (성공 시 데이터 전달, 실패 시 null)")
    private final T data;

    @Schema(description = "응답 메시지")
    private final String message;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(null)
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message("CREATED")
                .build();
    }
}
