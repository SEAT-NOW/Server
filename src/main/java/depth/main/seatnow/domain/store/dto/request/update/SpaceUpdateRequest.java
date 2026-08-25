package depth.main.seatnow.domain.store.dto.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "공간 및 좌석 정보 수정 요청")
@Getter
@NoArgsConstructor
public class SpaceUpdateRequest {
    @Schema(description = "공간 ID (신규 추가 시 null)", example = "1")
    private Long id;

    @Schema(description = "구역 이름", example = "1층 메인홀")
    private String name;

    @Schema(description = "해당 구역의 테이블 설정")
    @Valid
    private List<TableUpdateDto> tables;

    @Getter @NoArgsConstructor
    public static class TableUpdateDto {
        @Schema(description = "테이블 설정 ID (신규 추가 시 null)", example = "10")
        private Long id;

        @Schema(description = "테이블 타입 (인석)", example = "2")
        @NotNull
        private Integer tableType;

        @Schema(description = "해당 타입 테이블 개수", example = "10")
        @NotNull
        private Integer tableCount;
    }
}
