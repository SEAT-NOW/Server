package depth.main.seatnow.domain.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class BusinessRequest {
    @NotBlank(message = "대표자 성함은 필수입니다.")
    private String representativeName;

    @NotBlank(message = "사업자 등록번호는 필수입니다.")
    private String businessNumber;

    @NotBlank(message = "매장 이름은 필수입니다.")
    private String storeName;

    @NotBlank(message = "매장 주소는 필수입니다.")
    private String address;

    @NotEmpty(message = "최소 하나 이상의 대학교를 선택해야 합니다.")
    private List<String> universityNames;

    @NotBlank(message = "매장 전화번호는 필수입니다.")
    private String storePhone;

    private String businessLicenseUrl;
}
