package depth.main.seatnow.domain.store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SpaceRequest {
    private String name; // 예: "1층", "테라스"
    private List<TableConfigDto> tables;

    @Getter @NoArgsConstructor
    public static class TableConfigDto {
        @NotNull
        private Integer tableType;  // 2인석, 4인석 등

        @NotNull
        private Integer tableCount; // 테이블 개수
    }
}
