package depth.main.seatnow.infrastructure.external.google.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GoogleNearbySearchRequest {
    private List<String> includedTypes;
    private Integer maxResultCount;
    private LocationRestriction locationRestriction;

    @Getter @Builder
    public static class LocationRestriction {
        private Circle circle;
    }

    @Getter @Builder
    public static class Circle {
        private Center center;
        private Double radius;
    }

    @Getter @Builder
    public static class Center {
        private Double latitude;
        private Double longitude;
    }
}
