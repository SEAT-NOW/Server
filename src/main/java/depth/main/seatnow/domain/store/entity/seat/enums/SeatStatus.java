package depth.main.seatnow.domain.store.entity.seat.enums;

import lombok.Getter;

@Getter
public enum SeatStatus {
    FREE("여유"),      // 0 ~ 33%
    NORMAL("보통"),    // 34 ~ 66%
    CROWDED("혼잡"),   // 67 ~ 99%
    FULL("만석");      // 100%
    private final String description;
    SeatStatus(String description) { this.description = description; }
}
