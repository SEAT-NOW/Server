package depth.main.seatnow.domain.store.entity.store;

import depth.main.seatnow.domain.store.entity.menu.MenuCategory;
import depth.main.seatnow.domain.store.entity.operation.OpeningHour;
import depth.main.seatnow.domain.store.entity.operation.OperationStatus;
import depth.main.seatnow.domain.store.entity.operation.RegularHoliday;
import depth.main.seatnow.domain.store.entity.operation.TemporaryHoliday;
import depth.main.seatnow.domain.store.entity.seat.Space;
import depth.main.seatnow.domain.store.entity.seat.enums.SeatStatus;
import depth.main.seatnow.domain.user.entity.User;
import depth.main.seatnow.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private String neighborhood; //읍면동 단위 지역명 (예: 역북동, 서교동)

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

    @Enumerated(EnumType.STRING)
    private OperationStatus operationStatus; // 영업중, 곧 영업종료, 영업종료

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

    public void updateOperationStatus(LocalDateTime now) {
        OperationStatus status = OperationStatus.CLOSED;

        // 현재 날짜와 어제 날짜를 구분
        LocalDate todayDate = now.toLocalDate();
        LocalDate yesterdayDate = todayDate.minusDays(1);

        // 현재 요일과 어제 요일을 구함
        DayOfWeek todayDay = now.getDayOfWeek();
        DayOfWeek yesterdayDay = todayDay.minus(1);

        for (OpeningHour hour : this.openingHours) {
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;

            if (hour.getDayOfWeek() == todayDay) {
                LocalTime start = hour.getStartTime();
                LocalTime end = hour.getEndTime();

                // 영업 시작 시간 설정
                startDateTime = LocalDateTime.of(todayDate, start);

                if (end.isBefore(start)) {
                    // 자정을 넘겨 내일까지 영업하는 경우
                    endDateTime = LocalDateTime.of(todayDate.plusDays(1), end);
                } else {
                    // 오늘 안에 영업이 끝나는 경우
                    endDateTime = LocalDateTime.of(todayDate, end);
                }
            } else if (hour.getDayOfWeek() == yesterdayDay) {
                LocalTime start = hour.getStartTime();
                LocalTime end = hour.getEndTime();

                // 어제 영업이 자정을 넘긴 경우만 체크
                if (end.isBefore(start)) {
                    startDateTime = LocalDateTime.of(yesterdayDate, start);
                    endDateTime = LocalDateTime.of(todayDate, end);
                }
            }

            if (startDateTime != null) {
                if ((now.isEqual(startDateTime) || now.isAfter(startDateTime)) && now.isBefore(endDateTime)) {
                    status = OperationStatus.OPEN;

                    // 마감 1시간 전인지 확인
                    long minutesUntilClose = java.time.temporal.ChronoUnit.MINUTES.between(now, endDateTime);
                    if (minutesUntilClose <= 30) {
                        status = OperationStatus.CLOSING_SOON;
                    }

                    break;
                }
            }
        }

        this.operationStatus = status;
    }

    public void updateUsedSeatCount(int totalUsedSeats) {
        this.usedSeatCount = totalUsedSeats;
    }
}
