package depth.main.seatnow.domain.store.scheduler;

import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatResetScheduler {

    private final StoreRepository storeRepository;

    /**
     * 1분마다 실행됨, 현재 시각을 기준으로 영업 종료시간에 도달한 매장 좌석 초기화
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void resetSeatCountAtClosingTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES);
        DayOfWeek businessDay = calculateBusinessDay(now);

        List<Store> closingStores = storeRepository.findStoresByClosingTime(businessDay, currentTime);

        for (Store store : closingStores) {
            try {
                store.resetAllSeats();
                log.info("매장 ID: {} 좌석 초기화 완료", store.getId());
            } catch (Exception e) {
                log.error("매장 ID: {} 좌석 초기화 중 오류 발생 - {}", store.getId(), e.getMessage(), e);
            }
        }
    }

    private DayOfWeek calculateBusinessDay(LocalDateTime currentDateTime) {
        LocalTime time = currentDateTime.toLocalTime();
        if (time.isBefore(LocalTime.of(6, 0))) {
            return currentDateTime.minusDays(1).getDayOfWeek();
        }
        return currentDateTime.getDayOfWeek();
    }
}