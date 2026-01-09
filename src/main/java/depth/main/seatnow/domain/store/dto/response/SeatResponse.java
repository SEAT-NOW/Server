package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.seat.Space;
import depth.main.seatnow.domain.store.entity.seat.TableConfig;
import depth.main.seatnow.domain.store.entity.store.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "실시간 좌석 현황 응답 DTO")
public class SeatResponse {
    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "매장 상태 태그 (영문)", example = "CROWDED")
    private String statusTag; // 영문코드

    @Schema(description = "매장 상태 태그 이름 (한글)", example = "혼잡")
    private String statusTagName; // 한글설명

    @Schema(description = "전체 좌석 수", example = "40")
    private Integer totalSeatCount;    // 전체 좌석

    @Schema(description = "현재 이용 중인 총 좌석 수", example = "15")
    private Integer totalUsedSeatCount; // 이용 중 좌석 (이용 탭 시 이용)

    @Schema(description = "현재 비어 있는 총 좌석 수", example = "25")
    private Integer totalEmptySeatCount; // 빈 좌석 (빈 탭 시 이용)

    @Schema(description = "공간별 좌석 상세 리스트")
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
    @Schema(description = "공간별 좌석 상세 정보")
    public static class SpaceResponseDto {
        @Schema(description = "공간 ID", example = "1")
        private Long spaceId;

        @Schema(description = "공간 이름", example = "1층 메인홀")
        private String spaceName;

        @Schema(description = "해당 공간의 테이블 설정 리스트")
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
    @Schema(description = "테이블 타입별 상세 정보")
    public static class TableResponseDto {
        @Schema(description = "테이블 설정 ID", example = "10")
        private Long tableConfigId;

        @Schema(description = "테이블 타입 (n인석)", example = "4")
        private Integer tableType;  // n인석

        @Schema(description = "해당 타입의 전체 테이블 개수", example = "10")
        private Integer tableCount; // 테이블 총 개수

        @Schema(description = "현재 이용 중인 테이블 개수", example = "3")
        private Integer usedCount;  // 이용 중인 테이블 수

        @Schema(description = "현재 비어 있는 테이블 개수", example = "7")
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
