package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.operation.OpeningHour;
import depth.main.seatnow.domain.store.entity.operation.OperationStatus;
import depth.main.seatnow.domain.store.entity.operation.RegularHoliday;
import depth.main.seatnow.domain.store.entity.operation.TemporaryHoliday;
import depth.main.seatnow.domain.store.entity.store.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "매장 운영 정보 응답")
public class OperationInfoResponse {

    @Schema(description = "현재 운영 상태", example = "OPEN / CLOSED / BREAK_TIME")
    private OperationStatus operationStatus;

    @Schema(description = "정기 휴무 정보 리스트")
    private List<RegularHolidayDto> regularHolidays;

    @Schema(description = "임시 휴무 정보 리스트")
    private List<TemporaryHolidayDto> temporaryHolidays;

    @Schema(description = "영업 시간 정보 리스트")
    private List<OpeningHourDto> openingHours;

    public static OperationInfoResponse of(Store store) {
        return OperationInfoResponse.builder()
                .operationStatus(store.getOperationStatus())
                .openingHours(store.getOpeningHours().stream()
                        .map(OpeningHourDto::from)
                        .toList())
                .regularHolidays(store.getRegularHolidays().stream()
                        .map(RegularHolidayDto::from)
                        .toList())
                .temporaryHolidays(store.getTemporaryHolidays().stream()
                        .map(TemporaryHolidayDto::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "영업 시간 상세 정보")
    public static class OpeningHourDto {
        @Schema(description = "영업 시간 ID", example = "1")
        private Long id;

        @Schema(description = "영업 요일", example = "MONDAY")
        private DayOfWeek dayOfWeek;

        @Schema(description = "오픈 시간", example = "10:00:00")
        private LocalTime startTime;

        @Schema(description = "마감 시간", example = "22:00:00")
        private LocalTime endTime;

        public static OpeningHourDto from(OpeningHour entity) {
            return OpeningHourDto.builder()
                    .id(entity.getId())
                    .dayOfWeek(entity.getDayOfWeek())
                    .startTime(entity.getStartTime())
                    .endTime(entity.getEndTime())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "정기 휴무 상세 정보")
    public static class RegularHolidayDto {
        @Schema(description = "정기 휴무 ID", example = "1")
        private Long id;

        @Schema(description = "휴무 요일", example = "SUNDAY")
        private DayOfWeek dayOfWeek;

        @Schema(description = "휴무 주차 (0: 매주, 1~5: 해당 주차, 10: 마지막주)", example = "0")
        private Integer weekInfo;

        public static RegularHolidayDto from(RegularHoliday entity) {
            return RegularHolidayDto.builder()
                    .id(entity.getId())
                    .dayOfWeek(entity.getDayOfWeek())
                    .weekInfo(entity.getWeekInfo())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "임시 휴무 상세 정보")
    public static class TemporaryHolidayDto {
        @Schema(description = "임시 휴무 ID", example = "1")
        private Long id;

        @Schema(description = "휴무 시작일", example = "2026-01-01")
        private LocalDate startDate;

        @Schema(description = "휴무 종료일", example = "2026-01-02")
        private LocalDate endDate;

        public static TemporaryHolidayDto from(TemporaryHoliday entity) {
            return TemporaryHolidayDto.builder()
                    .id(entity.getId())
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .build();
        }
    }
}
