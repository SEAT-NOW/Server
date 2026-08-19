package depth.main.seatnow.domain.calendar.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class CalendarDto {

    @Getter
    @Builder
    public static class Response {
        private String yearMonth;
        private Map<String, String> dailyTags;
        private List<Event> eventList;
    }

    @Getter
    @Builder
    public static class Event {
        private String eventName;
        private String startDate;
        private String endDate;
        private String tag;
        private String description;
    }
}
