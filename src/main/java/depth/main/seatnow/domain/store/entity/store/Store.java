package depth.main.seatnow.domain.store.entity.store;

import depth.main.seatnow.domain.store.entity.menu.MenuCategory;
import depth.main.seatnow.domain.store.entity.operation.OpeningHour;
import depth.main.seatnow.domain.store.entity.operation.RegularHoliday;
import depth.main.seatnow.domain.store.entity.operation.TemporaryHoliday;
import depth.main.seatnow.domain.store.entity.seat.Space;
import depth.main.seatnow.domain.store.entity.seat.enums.SeatStatus;
import depth.main.seatnow.domain.user.entity.User;
import depth.main.seatnow.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Store extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     @OneToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id")
     private User user;

    @Column(nullable = false)
    private String representativeName; // 대표자명

    @Column(nullable = false, unique = true)
    private String businessNumber; // 사업자등록번호

    @Column(nullable = false)
    private String storeName; // 상호명

    @Column(nullable = false)
    private String address; // 주소

    @Column(nullable = false)
    private Double latitude; // 위도

    @Column(nullable = false)
    private Double longitude; // 경도

    @ElementCollection // 1:N 관계의 단순 문자열 리스트를 저장
    @CollectionTable(
            name = "store_university",
            joinColumns = @JoinColumn(name = "store_id")
    )
    @Column(name = "university_name")
    @Builder.Default
    private List<String> universityNames = new ArrayList<>();

    @Column(nullable = false)
    private String storePhone; // 가게 연락처

    private String businessLicenseUrl; // 사업자등록증 파일 경로 (S3 URL)

    @Column(nullable = false)
    private Integer totalSeatCount; // 매장의 전체 좌석 수

    @Column(nullable = false)
    private Integer usedSeatCount; // 현재 사용 중인 총 좌석 수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus statusTag; // 여유, 보통, 혼잡, 만석

    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreImage> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Space> spaces = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuCategory> menuCategories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpeningHour> openingHours = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegularHoliday> regularHolidays = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TemporaryHoliday> temporaryHolidays = new ArrayList<>();

    // 현재 가용 좌석 수 (인원수 필터링용)
    public int getAvailableSeatCount() {
        return this.totalSeatCount - this.usedSeatCount;
    }

    // 점유율 계산 및 태그 업데이트
    public void updateStatusTag() {
        double rate = (double) this.usedSeatCount / this.totalSeatCount * 100;

        if (rate >= 100) this.statusTag = SeatStatus.FULL;
        else if (rate >= 67) this.statusTag = SeatStatus.CROWDED;
        else if (rate >= 34) this.statusTag = SeatStatus.NORMAL;
        else this.statusTag = SeatStatus.FREE;
    }

    // 좌석 관련 정보 초기화
    public void initializeSeatInfo(int totalSeats) {
        this.totalSeatCount = totalSeats;     // 계산된 전체 좌석 수
        this.usedSeatCount = 0;               // 초기 가입 시 사용 중인 좌석은 0개
        this.statusTag = SeatStatus.FREE;     // 0%이므로 초기 상태는 '여유'
    }

    public void updateUsedSeatCount(int totalUsedSeats) {
        this.usedSeatCount = totalUsedSeats;
    }
}
