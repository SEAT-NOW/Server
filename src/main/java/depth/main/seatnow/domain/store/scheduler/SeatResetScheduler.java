package depth.main.seatnow.domain.store.scheduler;

import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.repository.StoreRepository;
import depth.main.seatnow.domain.store.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatResetScheduler {

    private final StoreRepository storeRepository;
    private final SeatService seatService;

    /**
     * 1분마다 실행되며, 현재 시각 기준 영업이 종료되었으나 좌석이 남아있는 매장의 좌석을 초기화함
     */
    @Scheduled(cron = "0 * * * * *")
    public void resetSeatCountAtClosingTime() {
        LocalDateTime now = LocalDateTime.now();
        List<Store> storesWithSeats = storeRepository.findStoresWithUsedSeats();

        for (Store store : storesWithSeats) {
            try {
                boolean reset = seatService.resetStoreSeatsIfClosed(store.getId(), now);
                if (reset) {
                    log.info("영업 마감 매장 ID: {} 좌석 초기화 완료", store.getId());
                }
            } catch (Exception e) {
                log.error("매장 ID: {} 좌석 초기화 중 오류 발생 - {}", store.getId(), e.getMessage(), e);
            }
        }
    }
}