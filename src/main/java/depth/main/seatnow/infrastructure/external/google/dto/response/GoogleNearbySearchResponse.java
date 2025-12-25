package depth.main.seatnow.infrastructure.external.google.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleNearbySearchResponse {
    @JsonProperty("places")
    private List<Place> places;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Place {
        @JsonProperty("id")
        private String id;

        @JsonProperty("displayName")
        private DisplayName displayName;

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DisplayName {
            @JsonProperty("text")
            private String text;
        }
    }
}
