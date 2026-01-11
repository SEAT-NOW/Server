package depth.main.seatnow.domain.place.service;

import depth.main.seatnow.domain.place.dto.response.PlaceSearchItemResponse;
import depth.main.seatnow.infrastructure.external.kakao.KakaoLocalClient;
import depth.main.seatnow.infrastructure.external.kakao.dto.response.KakaoKeywordSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {
    private final KakaoLocalClient kakaoLocalClient;

    public List<PlaceSearchItemResponse> searchByKeyword(String query, int page, int size) {
        KakaoKeywordSearchResponse res = kakaoLocalClient.searchKeyword(query, page, size);
        if (res.getDocuments() == null) return List.of();

        return res.getDocuments().stream()
                .map(doc -> {
                    String depth3 = "";

                    // 1. 상세 객체의 region_3depth_name 확인
                    if (doc.getAddress() != null && doc.getAddress().getRegion3DepthName() != null
                            && !doc.getAddress().getRegion3DepthName().isBlank()) {
                        depth3 = doc.getAddress().getRegion3DepthName();
                    }
                    // 2. 상세 객체가 없거나 부족할 때 문자열 파싱
                    else if (doc.getAddressName() != null && !doc.getAddressName().isBlank()) {
                        String[] parts = doc.getAddressName().split(" ");

                        if (parts.length >= 3) {
                            // 만약 index 2가 '구'로 끝난다면 '동'은 index 3에 있음
                            if (parts[2].endsWith("구") && parts.length >= 4) {
                                depth3 = parts[3];
                            } else {
                                depth3 = parts[2];
                            }
                        }
                    }

                    return PlaceSearchItemResponse.builder()
                            .name(doc.getPlaceName())
                            .roadAddress(doc.getRoadAddressName())
                            .neighborhood(depth3)
                            .lng(parseDoubleOrNull(doc.getX()))
                            .lat(parseDoubleOrNull(doc.getY()))
                            .build();
                })
                .filter(item -> item.getRoadAddress() != null && !item.getRoadAddress().isBlank())
                .filter(item -> item.getLat() != null && item.getLng() != null)
                .toList();
    }

    private Double parseDoubleOrNull(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
