package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.store.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "가게 기초 정보 조회 응답 (마이페이지)")
public class StoreProfileResponse {
    @Schema(description = "대표자명", example = "홍길동")
    private String representativeName;

    @Schema(description = "사업자 등록번호", example = "0000000000")
    private String businessNumber;

    @Schema(description = "상호명", example = "용용선생")
    private String storeName;

    @Schema(description = "가게 주소", example = "서울특별시 종로구 대학로8가길 36 1층")
    private String address;

    @Schema(description = "주변 대학명 리스트")
    private List<String> universityNames;

    @Schema(description = "사업자등록증 파일명", example = "license.png")
    private String businessLicenseFileName;

    @Schema(description = "가게 연락처", example = "021234567")
    private String storePhone;

    public static StoreProfileResponse of(Store store) {
        // S3 URL에서 마지막 '/' 이후의 파일명만 추출하는 로직
        String fullUrl = store.getBusinessLicenseUrl();
        String fileName = (fullUrl != null && fullUrl.contains("/"))
                ? fullUrl.substring(fullUrl.lastIndexOf("/") + 1)
                : fullUrl;

        return StoreProfileResponse.builder()
                .representativeName(store.getRepresentativeName())
                .businessNumber(store.getBusinessNumber())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .universityNames(store.getUniversityNames())
                .businessLicenseFileName(fileName)
                .storePhone(store.getStorePhone())
                .build();
    }
}
