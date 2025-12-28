package depth.main.seatnow.domain.store.entity.operation;

import depth.main.seatnow.domain.store.entity.store.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OpeningHour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek; // 영업 요일

    @Column(nullable = false)
    private LocalTime startTime; // 오픈 시간

    @Column(nullable = false)
    private LocalTime endTime;   // 마감 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
}
