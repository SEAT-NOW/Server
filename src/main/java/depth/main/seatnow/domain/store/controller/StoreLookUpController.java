package depth.main.seatnow.domain.store.controller;

import depth.main.seatnow.domain.store.dto.response.StoreListResponse;
import depth.main.seatnow.domain.store.service.StoreLookUpService;
import depth.main.seatnow.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "매장 조회", description = "일반 사용자용 매장 검색 및 상세 정보 조회 API")
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreLookUpController {
    private final StoreLookUpService storeService;
    @Operation(
            summary = "술집 검색 및 지도 조회",
            description = "홈 화면, N명 자리 찾기, 키워드 검색 API" +
                    "headCount가 1이상이면 좌석 수로 필터링, 결과는 최신 업데이트 순으로 정렬"
    )
    @GetMapping("/search")
    public ApiResponse<List<StoreListResponse>> searchStores(
            @Parameter(description = "검색어 (가게명 or 주소)")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "위도")
            @RequestParam(required = false) Double lat,

            @Parameter(description = "경도")
            @RequestParam(required = false) Double lng,

            @Parameter(description = "검색 반경 (km 단위, 기본값 1.0)")
            @RequestParam(defaultValue = "1.0") Double radius,

            @Parameter(description = "인원수 필터 (0: 전체, N: N석 이상 남은 곳")
            @RequestParam(defaultValue = "0") Integer headCount) {
        List<StoreListResponse> response = storeService.searchStores(keyword, lat, lng, radius, headCount);
        return ApiResponse.ok(response, "조회에 성공하였습니다.");
    }
}
