package depth.main.seatnow.infrastructure.external.kakao.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoKeywordSearchResponse {
    private List<Document> documents;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {

        @JsonProperty("place_name")
        private String placeName;

        @JsonProperty("road_address_name")
        private String roadAddressName;

        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("address")
        private Address address;

        // 카카오: x=경도(lng), y=위도(lat)
        @JsonProperty("x")
        private String x;

        @JsonProperty("y")
        private String y;

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Address {
            @JsonProperty("region_3depth_name")
            private String region3DepthName;

        }
    }

}
