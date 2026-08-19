package depth.main.seatnow.domain.calendar.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import depth.main.seatnow.domain.calendar.dto.CalendarDto;
import depth.main.seatnow.infrastructure.external.google.GoogleSheetsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalendarSyncService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final GoogleSheetsClient googleSheetsClient;

    private static final String REDIS_CALENDAR_KEY_PREFIX = "calendar:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 매일 새벽 4시에 구글 시트 데이터를 동기화하여 Redis에 저장
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void syncCalendarDataToRedis() {
        try {
            List<List<Object>> sheetData = googleSheetsClient.getCalendarData();

            List<CalendarDto.Event> allEvents = parseSheetDataToEvents(sheetData);

            Map<YearMonth, List<CalendarDto.Event>> eventsByMonth = allEvents.stream()
                    .collect(Collectors.groupingBy(event -> {
                        LocalDate start = LocalDate.parse(event.getStartDate(), DATE_FORMATTER);
                        return YearMonth.from(start);
                    }));

            for (Map.Entry<YearMonth, List<CalendarDto.Event>> entry : eventsByMonth.entrySet()) {
                String yearMonthStr = entry.getKey().toString();
                List<CalendarDto.Event> monthlyEvents = entry.getValue();

                Map<String, String> dailyTags = calculateDailyTags(monthlyEvents);

                CalendarDto.Response response = CalendarDto.Response.builder()
                        .yearMonth(yearMonthStr)
                        .dailyTags(dailyTags)
                        .eventList(monthlyEvents)
                        .build();

                String jsonValue = objectMapper.writeValueAsString(response);
                redisTemplate.opsForValue().set(
                        REDIS_CALENDAR_KEY_PREFIX + yearMonthStr,
                        jsonValue,
                        25,
                        TimeUnit.HOURS
                );
            }
            log.info("학사 캘린더 데이터 동기화 완료");

        } catch (Exception e) {
            log.error("캘린더 동기화 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 우선순위(HIGH > LOW > HOLIDAY)에 따라 날짜별 대표 태그 계산
     */
    private Map<String, String> calculateDailyTags(List<CalendarDto.Event> events) {
        Map<String, String> dailyTags = new HashMap<>();

        for (CalendarDto.Event event : events) {
            LocalDate startDate = LocalDate.parse(event.getStartDate(), DATE_FORMATTER);
            LocalDate endDate = event.getEndDate() == null || event.getEndDate().isBlank()
                    ? startDate
                    : LocalDate.parse(event.getEndDate(), DATE_FORMATTER);

            startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
                String dateStr = date.format(DATE_FORMATTER);
                String currentTag = dailyTags.get(dateStr);
                String newTag = event.getTag().toUpperCase();

                if (currentTag == null || getPriority(newTag) > getPriority(currentTag)) {
                    dailyTags.put(dateStr, newTag);
                }
            });
        }
        return dailyTags;
    }

    private int getPriority(String tag) {
        return switch (tag) {
            case "HIGH" -> 3;
            case "LOW" -> 2;
            case "HOLIDAY" -> 1;
            default -> 0;
        };
    }

    private List<CalendarDto.Event> parseSheetDataToEvents(List<List<Object>> sheetData) {
        List<CalendarDto.Event> events = new ArrayList<>();
        for (int i = 1; i < sheetData.size(); i++) {
            List<Object> row = sheetData.get(i);
            if (row.isEmpty()) continue;

            events.add(CalendarDto.Event.builder()
                    .startDate(getSafeString(row, 1))
                    .endDate(getSafeString(row, 2))
                    .tag(getSafeString(row, 3))
                    .eventName(getSafeString(row, 4))
                    .description(getSafeString(row, 5))
                    .build());
        }
        return events;
    }

    private String getSafeString(List<Object> row, int index) {
        return (row.size() > index && row.get(index) != null) ? row.get(index).toString() : null;
    }
}
