package depth.main.seatnow.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매장 좌석 현황 업데이트 요청 DTO")
public class SpaceSeatUpdateRequest {
    @Schema(description = "매장 ID", example = "1")
    @NotNull
    private Long storeId;

    @Schema(description = "공간별 업데이트 정보 리스트")
    @Valid
    private List<SpaceUpdateDto> spaceUpdates;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "공간별 업데이트 상세 정보")
    public static class SpaceUpdateDto {
        @Schema(description = "공간 ID", example = "1")
        @NotNull
        private Long spaceId;

        @Schema(description = "테이블 설정별 업데이트 정보 리스트")
        @Valid
        private List<TableUpdateDto> tableUpdates;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "테이블 타입별 업데이트 상세 정보")
    public static class TableUpdateDto {
        @Schema(description = "테이블 설정ID", example = "1")
        @NotNull
        private Long tableConfigId;

        @Schema(description = "현재 사용 중인 테이블 개수", example = "2", minimum = "0")
        @Min(value = 0, message = "테이블 수는 0개 이상이어야 합니다.")
        private int usedCount;
    }
}
