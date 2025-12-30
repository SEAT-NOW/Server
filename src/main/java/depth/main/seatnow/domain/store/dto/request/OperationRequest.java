package depth.main.seatnow.domain.store.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class OperationRequest {
    private List<RegularHolidayDto> regularHolidays;
    private List<TemporaryHolidayDto> temporaryHolidays;
    private List<OpeningHourDto> hours;

    @Getter
    @NoArgsConstructor
    public static class RegularHolidayDto {
        private DayOfWeek dayOfWeek;
        private Integer weekInfo; // 0:매주, 1-5:주차, 10:마지막주
    }

    @Getter
    @NoArgsConstructor
    public static class TemporaryHolidayDto {
        private LocalDate startDate;
        private LocalDate endDate;
    }
    @Getter
    @NoArgsConstructor
    public static class OpeningHourDto {
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
    }



}
