package depth.main.seatnow.domain.owner.service;

import depth.main.seatnow.global.exception.custom.BadRequestException;
import depth.main.seatnow.global.exception.custom.InternalServerException;
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

        // 1. 데이터 자체가 없는 경우 (서버 통신 장애)
        if (res.getData() == null || res.getData().isEmpty()) {
            throw new BadRequestException(ErrorCode.EXTERNAL_API_ERROR, "국세청 API 서버 응답이 없습니다.");
        }

        NtsBusinessResponse.NtsItem item = res.getData().get(0);
        String status = item.getB_stt_cd();

        // 2. 번호는 맞지만 상태가 부적절한 경우
        if (status == null || status.isEmpty()) {
            throw new InternalServerException(ErrorCode.INVALID_BUSINESS_NUMBER, "국세청에 등록되지 않은 사업자 번호입니다.");
        }

        if (!status.equals("01")) {
            String statusName = item.getB_stt(); // "폐업자" 또는 "휴업자"
            throw new BadRequestException(ErrorCode.INVALID_BUSINESS_NUMBER, "현재 " + statusName + " 상태입니다. 계속사업자만 등록 가능합니다.");
        }

        return true;
    }
}
