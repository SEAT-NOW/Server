package depth.main.seatnow.infrastructure.external.google;

import depth.main.seatnow.infrastructure.external.google.dto.request.GoogleNearbySearchRequest;
import depth.main.seatnow.infrastructure.external.google.dto.response.GoogleNearbySearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import depth.main.seatnow.global.exception.custom.InternalServerException;
import depth.main.seatnow.global.exception.error.ErrorCode;

@Component
@RequiredArgsConstructor
public class GooglePlacesClient {
    private final RestTemplate restTemplate;

    @Value("${google.places.base-url}")
    private String baseUrl;

    @Value("${google.places.api-key}")
    private String apiKey;

    public GoogleNearbySearchResponse searchNearby(GoogleNearbySearchRequest request, String fieldMask) {
        String url = baseUrl + "/v1/places:searchNearby";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 구글 인증/필드마스크
        headers.set("X-Goog-Api-Key", apiKey);
        headers.set("X-Goog-FieldMask", fieldMask);

        HttpEntity<GoogleNearbySearchRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<GoogleNearbySearchResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, GoogleNearbySearchResponse.class);

            // 응답은 왔으나 결과가 성공이 아니거나 본문이 비어있는 경우
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new InternalServerException(ErrorCode.EXTERNAL_API_ERROR);
            }

            return response.getBody();

        } catch (Exception e) {
            // 구글 서버 다운, 네트워크 타임아웃, 잘못된 필드 마스크 등 예기치 못한 모든 오류
            throw new InternalServerException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}
