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
        // 1. 기본 상태는 '영업 종료'로 설정
        OperationStatus status = OperationStatus.CLOSED;

        LocalDate todayDate = now.toLocalDate();
        DayOfWeek todayDay = now.getDayOfWeek();

        // 2. [검문소 1] 임시 휴무 체크
        for (TemporaryHoliday tempHoliday : this.temporaryHolidays) {
            // 오늘이 휴무 시작일과 종료일 사이에 있는지 확인
            if (!todayDate.isBefore(tempHoliday.getStartDate()) && !todayDate.isAfter(tempHoliday.getEndDate())) {
                this.operationStatus = OperationStatus.CLOSED;
                return; // 임시 휴무면 더 계산할 필요 없이 종료!
            }
        }

        // 3. [검문소 2] 정기 휴무 체크
        // 현재 주차 계산 (1~5주차)
        int weekOfMonth = (todayDate.getDayOfMonth() - 1) / 7 + 1;
        // 오늘로부터 7일 뒤의 달이 바뀌면 '마지막 주(10)'로 판정
        boolean isLastWeek = todayDate.plusDays(7).getMonthValue() != todayDate.getMonthValue();

        for (RegularHoliday regHoliday : this.regularHolidays) {
            if (regHoliday.getDayOfWeek() == todayDay) {
                int targetWeek = regHoliday.getWeekInfo(); // 0:매주, 1~5:해당주, 10:마지막주

                if (targetWeek == 0 || targetWeek == weekOfMonth || (targetWeek == 10 && isLastWeek)) {
                    this.operationStatus = OperationStatus.CLOSED;
                    return; // 정기 휴무면 더 계산할 필요 없이 종료!
                }
            }
        }

        // 4. [검문소 3] 영업시간 체크 (새벽 영업 포함)
        LocalDate yesterdayDate = todayDate.minusDays(1);
        DayOfWeek yesterdayDay = todayDay.minus(1);

        for (OpeningHour hour : this.openingHours) {
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;

            // 케이스 A: 오늘 요일 설정
            if (hour.getDayOfWeek() == todayDay) {
                LocalTime start = hour.getStartTime();
                LocalTime end = hour.getEndTime();
                startDateTime = LocalDateTime.of(todayDate, start);

                // 마감이 시작보다 빠르면(자정 넘김) 종료 날짜를 내일로 설정
                endDateTime = end.isBefore(start) ? LocalDateTime.of(todayDate.plusDays(1), end) : LocalDateTime.of(todayDate, end);
            }
            // 케이스 B: 어제 요일 설정 (어제 밤에 열어서 오늘 새벽에 닫는 경우)
            else if (hour.getDayOfWeek() == yesterdayDay) {
                LocalTime start = hour.getStartTime();
                LocalTime end = hour.getEndTime();
                if (end.isBefore(start)) {
                    startDateTime = LocalDateTime.of(yesterdayDate, start);
                    endDateTime = LocalDateTime.of(todayDate, end);
                }
            }

            // 현재 시간이 영업 범위(시작~마감) 안인지 최종 확인
            if (startDateTime != null && (now.isEqual(startDateTime) || now.isAfter(startDateTime)) && now.isBefore(endDateTime)) {
                status = OperationStatus.OPEN;

                // 마감 30분 전이면 '곧 영업 종료'로 표시
                long minutesUntilClose = java.time.temporal.ChronoUnit.MINUTES.between(now, endDateTime);
                if (minutesUntilClose <= 30) {
                    status = OperationStatus.CLOSING_SOON;
                }
                break; // 영업 중인 설정을 찾았으니 중단
            }
        }

        // 5. 최종 결정된 상태 저장
        this.operationStatus = status;
    }

    public void updateUsedSeatCount(int totalUsedSeats) {
        this.usedSeatCount = totalUsedSeats;
    }
}
