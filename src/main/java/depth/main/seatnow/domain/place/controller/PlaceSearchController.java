package depth.main.seatnow.domain.place.controller;

import depth.main.seatnow.domain.place.dto.response.PlaceSearchItemResponse;
import depth.main.seatnow.domain.place.service.PlaceSearchService;
import depth.main.seatnow.domain.place.service.UniversityCandidateService;
import depth.main.seatnow.global.common.ApiResponse;
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
public class PlaceSearchController {
    private final PlaceSearchService placeSearchService;
    private final UniversityCandidateService universityCandidateService;
    @GetMapping("/search")
    public ApiResponse<List<PlaceSearchItemResponse>> search(
            @RequestParam @NotBlank(message = "query는 필수입니다.") String query,
            @RequestParam(defaultValue = "1") @Min(1) @Max(45) int page,
            @RequestParam(defaultValue = "15") @Min(1) @Max(15) int size
    ) {
        List<PlaceSearchItemResponse> items = placeSearchService.searchByKeyword(query, page, size);

        return ApiResponse.ok(items);
    }

    @GetMapping("/universities")
    public ApiResponse<List<String>> getCandidates(
            @RequestParam @NotNull Double lat,
            @RequestParam @NotNull Double lng
    ) {
        List<String> names = universityCandidateService.getUniversityNames(lat, lng);
        if (names.isEmpty()) {
            return ApiResponse.ok(null, "비대학가");
        }
        return ApiResponse.ok(names);
    }

}
