package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.store.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매장 좌석 업데이트 응답 DTO")
public class SpaceSeatUpdateResponse {
    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "매장 전체 혼잡도 상태 (영문)", example = "FREE", allowableValues = {"FREE", "NORMAL", "CROWDED", "FULL"})
    private String statusTag; // 매장 전체 혼잡도 (FREE, NORMAL, CROWDED, FULL)

    @Schema(description = "매장 전체 혼잡도 상태 (한글)", example = "여유")
    private String statusTagName;

    @Schema(description = "매장 전체 좌석 수", example = "100")
    private Integer totalSeatCount;

    @Schema(description = "현재 사용 중인 총 좌석 수", example = "30")
    private Integer totalUsedSeatCount;

    @Schema(description = "현재 비어 있는 총 좌석 수", example = "70")
    private Integer totalEmptySeatCount;

    @Schema(description = "구역별 업데이트 결과 리스트")
    private List<SpaceResultDto> spaceResults;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "구역별 상세 결과")
    public static class SpaceResultDto {
        @Schema(description = "공간 ID", example = "10")
        private Long spaceId;

        @Schema(description = "공간 이름", example = "1층 테라스")
        private String spaceName;

        @Schema(description = "공간 내 테이블 설정 리스트")
        private List<TableResultDto> tables;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "테이블 타입별 상세 결과")
    public static class TableResultDto {
        @Schema(description = "테이블 ID", example = "101")
        private Long tableConfigId;

        @Schema(description = "테이블 타입 (인원수)", example = "2")
        private Integer tableType;

        @Schema(description = "해당 타입의 전체 테이블 개수", example = "10")
        private Integer tableCount;

        @Schema(description = "현재 사용 중인 테이블 개수", example = "3")
        private Integer usedCount;

        @Schema(description = "비어 있는 테이블 개수", example = "7")
        private Integer emptyCount;
    }
    public static SpaceSeatUpdateResponse from(Store store) {
        return SpaceSeatUpdateResponse.builder()
                .storeId(store.getId())
                .statusTag(store.getStatusTag().name())
                .statusTagName(store.getStatusTag().getDescription())
                .totalUsedSeatCount(store.getUsedSeatCount())
                .totalSeatCount(store.getTotalSeatCount())
                .totalEmptySeatCount(store.getTotalSeatCount() - store.getUsedSeatCount())
                .spaceResults(store.getSpaces().stream().map(space ->
                        SpaceResultDto.builder()
                                .spaceId(space.getId())
                                .spaceName(space.getName())
                                .tables(space.getTableConfigs().stream().map(table ->
                                        TableResultDto.builder()
                                                .tableConfigId(table.getId())
                                                .tableType(table.getTableType())
                                                .tableCount(table.getTableCount())
                                                .usedCount(table.getUsedCount())
                                                .emptyCount(table.getTableCount() - table.getUsedCount())
                                                .build()
                                ).toList())
                                .build()
                ).toList())
                .build();
    }
}
