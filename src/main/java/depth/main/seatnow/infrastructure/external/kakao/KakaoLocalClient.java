package depth.main.seatnow.infrastructure.external.kakao;

import depth.main.seatnow.global.exception.custom.InternalServerException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import depth.main.seatnow.infrastructure.external.kakao.dto.response.KakaoKeywordSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KakaoLocalClient {
    private final RestTemplate restTemplate;

    @Value("${kakao.local.base-url}")
    private String baseUrl;

    @Value("${kakao.rest-api-key}")
    private String restApiKey;

    public KakaoKeywordSearchResponse searchKeyword(String query, int page, int size) {
        // URI 생성
        URI uri = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/v2/local/search/keyword.json")
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("size", size)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + restApiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // HTTP 요청 및 응답 처리
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<KakaoKeywordSearchResponse> response =
                    restTemplate.exchange(uri, HttpMethod.GET, entity, KakaoKeywordSearchResponse.class);
            // 응답 본문이 비어있거나 2xx 코드가 아닐 경우
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new InternalServerException(ErrorCode.EXTERNAL_API_ERROR);
            }

            return response.getBody();

        } catch (Exception e) {
            // 네트워크 타임아웃, 커넥션 거부 등 예기치 못한 모든 외부 오류 처리
            throw new InternalServerException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

}
