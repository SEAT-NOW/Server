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
        private String b_no;
        private String b_stt;
        private String b_stt_cd;
        private String tax_type;
    }
}
