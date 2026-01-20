package depth.main.seatnow.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "매장 운영 정보 일괄 수정 요청")
@Getter
@NoArgsConstructor
public class OperationUpdateRequest {
    @Schema(description = "정기 휴무 수정 리스트")
    private List<RegularHolidayUpdateDto> regularHolidays;

    @Schema(description = "임시 휴무 수정 리스트")
    private List<TemporaryHolidayUpdateDto> temporaryHolidays;

    @Schema(description = "영업 시간 수정 리스트")
    private List<OpeningHourUpdateDto> hours;

    @Getter @NoArgsConstructor
    public static class RegularHolidayUpdateDto {
        @Schema(description = "정기 휴무 ID (신규 추가 시 null)", example = "1")
        private Long id; // 수정 시 필수, 추가 시 null

        @Schema(description = "휴무 요일", example = "MONDAY")
        private DayOfWeek dayOfWeek;

        @Schema(description = "휴무 주차 (0:매주, 1~5:해당 주차, 10:마지막 주)", example = "0")
        private Integer weekInfo;
    }

    @Getter @NoArgsConstructor
    public static class TemporaryHolidayUpdateDto {
        @Schema(description = "임시 휴무 ID (신규 추가 시 null)", example = "2")
        private Long id;

        @Schema(description = "휴무 시작일", example = "2026-01-20")
        private LocalDate startDate;

        @Schema(description = "휴무 종료일", example = "2026-01-21")
        private LocalDate endDate;
    }

    @Getter @NoArgsConstructor
    public static class OpeningHourUpdateDto {
        @Schema(description = "영업 시간 ID (신규 추가 시 null)", example = "3")
        private Long id;

        @Schema(description = "영업 요일", example = "TUESDAY")
        private DayOfWeek dayOfWeek;

        @Schema(description = "영업 시작 시간 (HH:mm)", example = "10:00")
        private LocalTime startTime;

        @Schema(description = "영업 종료 시간 (HH:mm)", example = "22:00")
        private LocalTime endTime;
    }

}
