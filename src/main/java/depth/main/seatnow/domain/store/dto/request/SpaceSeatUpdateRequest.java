package depth.main.seatnow.domain.store.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SpaceSeatUpdateRequest {
    private Long storeId;
    private List<SpaceUpdateDto> spaceUpdates;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpaceUpdateDto {
        private Long spaceId;
        private List<TableUpdateDto> tableUpdates;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableUpdateDto {
        private Long tableConfigId;
        private int usedCount;
    }
}
