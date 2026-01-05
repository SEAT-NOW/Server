package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.seat.Space;
import depth.main.seatnow.domain.store.entity.seat.TableConfig;
import depth.main.seatnow.domain.store.entity.store.Store;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SeatResponse {
    private Long storeId;
    private String statusTag; // 영문코드
    private String statusTagName; // 한글설명
    private Integer totalSeatCount;    // 전체 좌석
    private Integer totalUsedSeatCount; // 이용 중 좌석 (이용 탭 시 이용)
    private Integer totalEmptySeatCount; // 빈 좌석 (빈 탭 시 이용)
    private List<SpaceResponseDto> spaces;

    public static SeatResponse from(Store store) {
        return SeatResponse.builder()
                .storeId(store.getId())
                .statusTag(store.getStatusTag().name())
                .statusTagName(store.getStatusTag().getDescription())
                .totalSeatCount(store.getTotalSeatCount())
                .totalUsedSeatCount(store.getUsedSeatCount())
                .totalEmptySeatCount(store.getTotalSeatCount() - store.getUsedSeatCount())
                .spaces(store.getSpaces().stream()
                        .map(SpaceResponseDto::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    public static class SpaceResponseDto {
        private Long spaceId;
        private String spaceName;
        private List<TableResponseDto> tables;

        public static SpaceResponseDto from(Space space) {
            return SpaceResponseDto.builder()
                    .spaceId(space.getId())
                    .spaceName(space.getName())
                    .tables(space.getTableConfigs().stream()
                            .map(TableResponseDto::from)
                            .toList())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TableResponseDto {
        private Long tableConfigId;
        private Integer tableType;  // n인석
        private Integer tableCount; // 테이블 총 개수
        private Integer usedCount;  // 이용 중인 테이블 수
        private Integer emptyCount; // 비어 있는 테이블 수

        public static TableResponseDto from(TableConfig table) {
            return TableResponseDto.builder()
                    .tableConfigId(table.getId())
                    .tableType(table.getTableType())
                    .tableCount(table.getTableCount())
                    .usedCount(table.getUsedCount())
                    .emptyCount(table.getTableCount() - table.getUsedCount())
                    .build();
        }
    }

}
