package depth.main.seatnow.infrastructure.external.nts;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NtsBusinessClient {
    @Value("${external-api.nts-business.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public NtsBusinessResponse validateBusinessNumber(String number) {

        String url = "https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=" + apiKey;

        Map<String, Object> body = Map.of("b_no", List.of(number));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body);

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                NtsBusinessResponse.class
        ).getBody();
    }
}
