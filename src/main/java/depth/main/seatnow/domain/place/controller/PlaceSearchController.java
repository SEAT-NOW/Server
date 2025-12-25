package depth.main.seatnow.domain.place.controller;

import depth.main.seatnow.domain.place.dto.response.PlaceSearchItemResponse;
import depth.main.seatnow.domain.place.service.PlaceSearchService;
import depth.main.seatnow.domain.place.service.UniversityCandidateService;
import depth.main.seatnow.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/places")
@Tag(name = "Place Search", description = "장소 검색 및 위치 분석 API")
public class PlaceSearchController {
    private final PlaceSearchService placeSearchService;
    private final UniversityCandidateService universityCandidateService;
    @Operation(
            summary = "키워드 장소 검색",
            description = "카카오 로컬 API를 사용하여 키워드로 장소를 검색합니다. (도로명 주소와 좌표가 있는 데이터만 반환)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = "{\"success\": true, \"data\": [{\"name\": \"시트나우 카페\", \"roadAddress\": \"서울특별시 강남구 테헤란로 123\", \"lng\": 127.027, \"lat\": 37.497}], \"message\": \"요청에 성공하였습니다.\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "외부 API 호출 실패 (EXTERNAL_API_ERROR)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "에러 예시",
                                    value = "{\"code\": \"5001\", \"message\": \"외부 시스템과의 통신 중 오류가 발생했습니다.\"}"
                            )
                    )
            )
    })
    @GetMapping("/search")
    public ApiResponse<List<PlaceSearchItemResponse>> search(
            @Parameter(description = "검색어 (예: 스타벅스 강남)", required = true)
            @RequestParam @NotBlank(message = "query는 필수입니다.") String query,

            @Parameter(description = "페이지 번호 (1~45)")
            @RequestParam(defaultValue = "1") @Min(1) @Max(45) int page,

            @Parameter(description = "한 페이지 결과 수 (최대 15)")
            @RequestParam(defaultValue = "15") @Min(1) @Max(15) int size
    ) {
        List<PlaceSearchItemResponse> items = placeSearchService.searchByKeyword(query, page, size);

        return ApiResponse.ok(items, "요청에 성공하였습니다.");
    }

    @Operation(
            summary = "주변 대학교 조회",
            description = "특정 좌표(위경도)를 기준으로 반경 내에 위치한 대학교 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "대학가인 경우",
                                            value = "{\"success\": true, \"data\": [\"서울대학교\", \"연세대학교\"], \"message\": \"요청에 성공하였습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "비대학가인 경우",
                                            value = "{\"success\": true, \"data\": null, \"message\": \"비대학가\"}"
                                    )
                            }
                    )
            )
    })
    @GetMapping("/universities")
    public ApiResponse<List<String>> getCandidates(
            @Parameter(description = "위도 (Latitude)", example = "37.497942")
            @RequestParam @NotNull Double lat,

            @Parameter(description = "경도 (Longitude)", example = "127.027610")
            @RequestParam @NotNull Double lng
    ) {
        List<String> names = universityCandidateService.getUniversityNames(lat, lng);
        if (names.isEmpty()) {
            return ApiResponse.ok(null, "비대학가");
        }
        return ApiResponse.ok(names, "요청에 성공하였습니다.");
    }

}
