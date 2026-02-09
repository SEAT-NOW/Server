package depth.main.seatnow.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
@Getter
@Builder
@Schema(description = "통합 검색 응답 (가게 리스트 + 연관 대학 리스트)")
public class StoreSearchResponse {
    @Schema(description = "키워드와 연관된 대학교 목록 (돋보기 리스트용)")
    private List<String> relatedUniversities;

    @Schema(description = "검색된 가게 목록")
    private List<StoreListResponse> stores;

    public static StoreSearchResponse of(List<StoreListResponse> stores, List<String> universities) {
        return StoreSearchResponse.builder()
                .stores(stores)
                .relatedUniversities(universities)
                .build();
    }
}
