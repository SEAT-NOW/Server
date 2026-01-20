package depth.main.seatnow.domain.store.dto.request.signup;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ImageRequest {
    private String imageUrl; // S3 temp URL
    private Boolean isPrimary; // 대표 사진 여부
}
