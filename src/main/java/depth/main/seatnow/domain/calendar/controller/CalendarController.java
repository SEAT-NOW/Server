package depth.main.seatnow.domain.calendar.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import depth.main.seatnow.domain.calendar.dto.CalendarDto;
import depth.main.seatnow.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v2/calendars")
@RequiredArgsConstructor
public class CalendarController {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Operation(summary = "월별 학사 캘린더 조회", description = "해당 월의 주요 학사 일정과 우선순위가 반영된 태그를 반환합니다.")
    @GetMapping
    public ApiResponse<CalendarDto.Response> getMonthlyCalendar(
            @RequestParam int year,
            @RequestParam int month) throws JsonProcessingException {

        String yearMonthStr = String.format("%04d-%02d", year, month);
        String redisKey = "calendar:" + yearMonthStr;

        String jsonValue = redisTemplate.opsForValue().get(redisKey);

        if (jsonValue == null) {
            return ApiResponse.ok(
                    CalendarDto.Response.builder().yearMonth(yearMonthStr).build(),
                    "해당 월의 캘린더 데이터가 없습니다."
            );
        }

        CalendarDto.Response response = objectMapper.readValue(jsonValue, CalendarDto.Response.class);
        return ApiResponse.ok(response);
    }
}
