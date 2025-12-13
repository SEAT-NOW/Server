package depth.main.seatnow.domain.owner.service;

import depth.main.seatnow.global.exception.custom.BadRequestException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import depth.main.seatnow.infrastructure.external.nts.NtsBusinessClient;
import depth.main.seatnow.infrastructure.external.nts.NtsBusinessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerBusinessService {
    private final NtsBusinessClient ntsBusinessClient;

    public boolean verifyBusinessNumber(String businessNumber) {

        NtsBusinessResponse res = ntsBusinessClient.validateBusinessNumber(businessNumber);

        if (res.getData().isEmpty()) {
            throw new BadRequestException(ErrorCode.INVALID_BUSINESS_NUMBER, "국세청 응답 없음");
        }

        String status = res.getData().get(0).getB_stt_cd();

        // 01 = 계속사업자
        if (!status.equals("01")) {
            throw new BadRequestException(ErrorCode.INVALID_BUSINESS_NUMBER, "폐업자 또는 휴업자");
        }

        return true;
    }
}
