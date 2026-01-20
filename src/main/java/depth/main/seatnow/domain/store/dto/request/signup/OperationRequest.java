package depth.main.seatnow.domain.store.dto.request.signup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "매장 운영 및 휴무 정보 설정")
@Getter
@NoArgsConstructor
public class OperationRequest {
    @Schema(description = "정기 휴무 리스트")
    private List<RegularHolidayDto> regularHolidays;

    @Schema(description = "임시 휴무 리스트")
    private List<TemporaryHolidayDto> temporaryHolidays;

    @Schema(description = "요일별 영업 시간 설정")
    private List<OpeningHourDto> hours;

    @Getter
    @NoArgsConstructor
    public static class RegularHolidayDto {
        @Schema(description = "정기 휴무 요일", example = "MONDAY")
        private DayOfWeek dayOfWeek;

        @Schema(description = "주차 정보 (0:매주, 1~5:해당 주차, 10:마지막 주)", example = "0")
        private Integer weekInfo; // 0:매주, 1-5:주차, 10:마지막주
    }

    @Getter
    @NoArgsConstructor
    public static class TemporaryHolidayDto {
        @Schema(description = "임시 휴무 시작일", example = "2024-12-25")
        private LocalDate startDate;

        @Schema(description = "임시 휴무 종료일", example = "2024-12-26")
        private LocalDate endDate;
    }
    @Getter
    @NoArgsConstructor
    public static class OpeningHourDto {
        @Schema(description = "영업 요일", example = "TUESDAY")
        private DayOfWeek dayOfWeek;

        @Schema(description = "영업 시작 시간 (HH:mm)", example = "10:00")
        private LocalTime startTime;

        @Schema(description = "영업 종료 시간 (HH:mm)", example = "22:00")
        private LocalTime endTime;
    }



}
