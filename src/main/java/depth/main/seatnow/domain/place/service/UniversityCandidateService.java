package depth.main.seatnow.domain.place.service;

import depth.main.seatnow.infrastructure.external.google.GooglePlacesClient;
import depth.main.seatnow.infrastructure.external.google.dto.request.GoogleNearbySearchRequest;
import depth.main.seatnow.infrastructure.external.google.dto.response.GoogleNearbySearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UniversityCandidateService {
    private final GooglePlacesClient googlePlacesClient;

    private static final int RADIUS_METERS = 2500;
    private static final int MAX_RESULT_COUNT = 20;

    // "OO대학교", "OO대학"까지만 추출 (한글 word-boundary(\b) 대신 lookahead 사용)
    private static final Pattern UNI_ROOT_PATTERN =
            Pattern.compile("^(.+?(대학교|대학))(?=\\s|\\(|$)");

    public List<String> getUniversityNames(double lat, double lng) {

        GoogleNearbySearchRequest req = GoogleNearbySearchRequest.builder()
                .includedTypes(List.of("university"))
                .maxResultCount(MAX_RESULT_COUNT)
                .locationRestriction(
                        GoogleNearbySearchRequest.LocationRestriction.builder()
                                .circle(GoogleNearbySearchRequest.Circle.builder()
                                        .center(GoogleNearbySearchRequest.Center.builder()
                                                .latitude(lat)
                                                .longitude(lng)
                                                .build())
                                        .radius((double) RADIUS_METERS)
                                        .build())
                                .build()
                )
                .build();

        GoogleNearbySearchResponse res = googlePlacesClient.searchNearby(
                req,
                "places.displayName"
        );

        if (res.getPlaces() == null) return List.of();

        // 대학명만 뽑고 중복 제거 후 정렬
        Set<String> names = new HashSet<>();
        for (GoogleNearbySearchResponse.Place p : res.getPlaces()) {
            if (p.getDisplayName() == null || p.getDisplayName().getText() == null) continue;

            String root = extractUniversityRoot(p.getDisplayName().getText());
            if (root != null) names.add(root);
        }

        List<String> result = new ArrayList<>(names);
        Collections.sort(result);
        return result;
    }

    private String extractUniversityRoot(String name) {
        if (name == null) return null;
        Matcher m = UNI_ROOT_PATTERN.matcher(name.trim());
        if (m.find()) return m.group(1).trim();
        return null;
    }
}
