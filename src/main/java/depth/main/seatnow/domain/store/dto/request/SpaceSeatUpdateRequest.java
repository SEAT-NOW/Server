package depth.main.seatnow.domain.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SpaceSeatUpdateRequest {
    @NotNull
    private Long storeId;
    private List<SpaceUpdateDto> spaceUpdates;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpaceUpdateDto {
        @NotNull
        private Long spaceId;
        private List<TableUpdateDto> tableUpdates;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableUpdateDto {
        @NotNull
        private Long tableConfigId;

        @Min(value = 0, message = "테이블 수는 0개 이상이어야 합니다.")
        private int usedCount;
    }
}
