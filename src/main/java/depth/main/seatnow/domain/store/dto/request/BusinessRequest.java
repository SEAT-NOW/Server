package depth.main.seatnow.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Schema(description = "매장 사업자 정보")
@Getter
@NoArgsConstructor
public class BusinessRequest {
    @Schema(description = "대표자 성함", example = "홍길동")
    @NotBlank(message = "대표자 성함은 필수입니다.")
    private String representativeName;

    @Schema(description = "사업자 등록번호", example = "123-45-67890")
    @NotBlank(message = "사업자 등록번호는 필수입니다.")
    private String businessNumber;

    @Schema(description = "매장 이름", example = "시트나우 카페 홍대점")
    @NotBlank(message = "매장 이름은 필수입니다.")
    private String storeName;

    @Schema(description = "매장 상세 주소", example = "서울시 마포구 와우산로 123")
    @NotBlank(message = "매장 주소는 필수입니다.")
    private String address;

    @Schema(description = "읍면동 단위 지역명", example = "역북동")
    @NotBlank(message = "지역명(동)은 필수입니다.")
    private String neighborhood;

    @Schema(description = "매장 위도", example = "127.027")
    @NotNull(message = "매장 위도는 필수입니다.")
    private Double latitude; // 위도

    @Schema(description = "매장 경도", example = "37.497")
    @NotNull(message = "매장 경도는 필수입니다.")
    private Double longitude; // 경도

    @Schema(description = "인근 대학교 이름 리스트", example = "[\"홍익대학교\", \"연세대학교\"]")
    @NotEmpty(message = "최소 하나 이상의 대학교를 선택해야 합니다.")
    private List<String> universityNames;

    @Schema(description = "매장 전화번호", example = "02-123-4567")
    @NotBlank(message = "매장 전화번호는 필수입니다.")
    private String storePhone;

}
