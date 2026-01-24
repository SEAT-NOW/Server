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

    @Column(nullable = true)
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

    private LocalDateTime seatModifiedAt; // 좌석 정보가 마지막으로 업데이트된 시각

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
        LocalDate todayDate = now.toLocalDate();
        DayOfWeek todayDay = now.getDayOfWeek();

        // 1. 임시 휴무 체크
        for (TemporaryHoliday tempHoliday : this.temporaryHolidays) {
            if (!todayDate.isBefore(tempHoliday.getStartDate()) && !todayDate.isAfter(tempHoliday.getEndDate())) {
                this.operationStatus = OperationStatus.CLOSED;
                return;
            }
        }

        // 2. 정기 휴무 체크
        int weekOfMonth = (todayDate.getDayOfMonth() - 1) / 7 + 1;
        boolean isLastWeek = todayDate.plusDays(7).getMonthValue() != todayDate.getMonthValue();

        for (RegularHoliday regHoliday : this.regularHolidays) {
            if (regHoliday.getDayOfWeek() == todayDay) {
                int targetWeek = regHoliday.getWeekInfo();
                if (targetWeek == 0 || targetWeek == weekOfMonth || (targetWeek == 10 && isLastWeek)) {
                    this.operationStatus = OperationStatus.CLOSED;
                    return;
                }
            }
        }

        // 3. 영업 시간 체크 (어제 설정과 오늘 설정을 모두 검사)
        for (OpeningHour hour : this.openingHours) {
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;

            // [오늘 요일 설정인 경우]
            if (hour.getDayOfWeek() == todayDay) {
                startDateTime = LocalDateTime.of(todayDate, hour.getStartTime());
                // 마감이 시작보다 빠르면(자정 넘김) 종료는 내일
                if (hour.getEndTime().isBefore(hour.getStartTime())) {
                    endDateTime = LocalDateTime.of(todayDate.plusDays(1), hour.getEndTime());
                } else {
                    endDateTime = LocalDateTime.of(todayDate, hour.getEndTime());
                }
            }
            // [어제 요일 설정인 경우]
            else if (hour.getDayOfWeek() == todayDay.minus(1)) {
                // 어제 설정이 자정을 넘기는 설정일 때만 '어제 시작 ~ 오늘 종료' 범위 생성
                if (hour.getEndTime().isBefore(hour.getStartTime())) {
                    startDateTime = LocalDateTime.of(todayDate.minusDays(1), hour.getStartTime());
                    endDateTime = LocalDateTime.of(todayDate, hour.getEndTime());
                }
            }

            // 현재 시간이 계산된 범위 안에 있는지 확인
            if (startDateTime != null && endDateTime != null) {
                if ((now.isEqual(startDateTime) || now.isAfter(startDateTime)) && now.isBefore(endDateTime)) {
                    status = OperationStatus.OPEN;

                    long minutesUntilClose = java.time.temporal.ChronoUnit.MINUTES.between(now, endDateTime);
                    if (minutesUntilClose <= 30) {
                        status = OperationStatus.CLOSING_SOON;
                    }
                    break; // 영업 중인 범위를 하나라도 찾으면 즉시 종료
                }
            }
        }

        this.operationStatus = status;
    }

    public void updateUsedSeatCount(int totalUsedSeats) {
        this.usedSeatCount = totalUsedSeats;
        this.seatModifiedAt = LocalDateTime.now();
    }

    public void updateStorePhone(String storePhone) {
        this.storePhone = storePhone;
    }
}
