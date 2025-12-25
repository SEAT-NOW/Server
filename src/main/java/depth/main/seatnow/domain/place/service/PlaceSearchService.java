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
                .map(doc -> PlaceSearchItemResponse.builder()
                        .name(doc.getPlaceName())
                        .roadAddress(doc.getRoadAddressName())
                        .lng(parseDoubleOrNull(doc.getX())) // x=경도
                        .lat(parseDoubleOrNull(doc.getY())) // y=위도
                        .build())
                // 유효하지 않은 데이터 필터링
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
