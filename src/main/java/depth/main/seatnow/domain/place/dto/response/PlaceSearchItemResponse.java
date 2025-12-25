package depth.main.seatnow.domain.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaceSearchItemResponse {
    @Schema(description = "장소명 (상호명)", example = "시트나우 카페")
    private String name;

    @Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 123")
    private String roadAddress;

    @Schema(description = "경도 (Longitude/x)", example = "127.027610")
    private Double lng;

    @Schema(description = "위도 (Latitude/y)", example = "37.497942")
    private Double lat;
}
