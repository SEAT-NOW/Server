package depth.main.seatnow.infrastructure.external.nts;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class NtsBusinessResponse {
    private List<NtsItem> data;

    @Getter
    @Setter
    public static class NtsItem {
        private String b_no; // 사업자 등록번호
        private String b_stt; // 사업자 상태(텍스트)
        private String b_stt_cd; // 사업자 상태 코드 01 : 계속, 02 : 휴업, 03 : 폐업
        private String tax_type; // 과세 유형
    }
}
