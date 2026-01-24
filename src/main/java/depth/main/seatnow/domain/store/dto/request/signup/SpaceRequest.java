package depth.main.seatnow.domain.store.dto.request.signup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Schema(description = "층별/구역별 좌석 레이아웃 리스트")
@Getter
@NoArgsConstructor
public class SpaceRequest {
    @Schema(description = "구역 이름", example = "1층 메인홀")
    private String name; // 예: "1층", "테라스"

    @Schema(description = "해당 구역의 테이블 설정")
    private List<TableConfigDto> tables;

    @Getter @NoArgsConstructor
    public static class TableConfigDto {
        @Schema(description = "테이블 타입 (인석)", example = "2")
        @NotNull
        private Integer tableType;  // 2인석, 4인석 등

        @Schema(description = "해당 타입 테이블 개수", example = "10")
        @NotNull
        private Integer tableCount; // 테이블 개수
    }
}
