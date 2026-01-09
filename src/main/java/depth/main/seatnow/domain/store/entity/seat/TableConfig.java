package depth.main.seatnow.domain.store.entity.seat;

import depth.main.seatnow.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TableConfig extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer tableType;

    @Column(nullable = false)
    private Integer tableCount; // 해당 타입의 테이블 개수

    @Builder.Default
    @Column(nullable = false)
    private Integer usedCount = 0; // 현재 사용 중인 테이블 개수 (실시간 업데이트용)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private Space space;

    public static TableConfig create(Integer type, Integer count, Space space) {
        return TableConfig.builder()
                .tableType(type)
                .tableCount(count)
                .space(space)
                .build();
    }

    public void updateUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }
}
