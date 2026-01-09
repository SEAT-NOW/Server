package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.store.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceSeatUpdateResponse {
    private Long storeId;
    private String statusTag; // 매장 전체 혼잡도 (FREE, NORMAL, CROWDED, FULL)
    private String statusTagName;
    private Integer totalSeatCount;
    private Integer totalUsedSeatCount;
    private Integer totalEmptySeatCount;
    private List<SpaceResultDto> spaceResults;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpaceResultDto {
        private Long spaceId;
        private String spaceName;
        private List<TableResultDto> tables;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableResultDto {
        private Long tableConfigId;
        private Integer tableType;
        private Integer tableCount;
        private Integer usedCount;
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
