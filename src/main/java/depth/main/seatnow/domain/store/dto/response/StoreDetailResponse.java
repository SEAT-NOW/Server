package depth.main.seatnow.domain.store.dto.response;

import depth.main.seatnow.domain.store.entity.menu.Menu;
import depth.main.seatnow.domain.store.entity.menu.MenuCategory;
import depth.main.seatnow.domain.store.entity.operation.OpeningHour;
import depth.main.seatnow.domain.store.entity.operation.OperationStatus;
import depth.main.seatnow.domain.store.entity.operation.RegularHoliday;
import depth.main.seatnow.domain.store.entity.operation.TemporaryHoliday;
import depth.main.seatnow.domain.store.entity.seat.enums.SeatStatus;
import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.entity.store.StoreImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;
import java.util.function.Function;

@Getter
@Builder
public class StoreDetailResponse {

    private Long storeId;

    private String storeName; // 매장 이름

    private String address; // 주소

    private String neighborhood; // 동,읍

    private List<String> universityNames; // 대학 이름

    private String storePhone; // 매장 전화

    private Integer totalSeatCount; // 전체 좌석 수
    private Integer usedSeatCount; // 사용중인 좌석 수
    private SeatStatus statusTag; // 혼잡도 상태

    private OperationStatus operationStatus; // 운영 정보

    private List<OpeningHourDto> openingHours; // 운영 시간
    private List<RegularHolidayDto> regularHolidays; // 휴일
    private List<TemporaryHolidayDto> temporaryHolidays; // 임시 휴일

    private List<ImageDto> images; // 가게 사진
    private List<MenuCategoryDto> menuCategories; // 메뉴 정보

    private boolean isKept;

    public static StoreDetailResponse from(Store store, boolean isKept) {
        return StoreDetailResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .neighborhood(store.getNeighborhood())
                .universityNames(store.getUniversityNames())
                .storePhone(store.getStorePhone())
                .totalSeatCount(store.getTotalSeatCount())
                .usedSeatCount(store.getUsedSeatCount())
                .statusTag(store.getStatusTag())
                .operationStatus(store.getOperationStatus())
                .openingHours(mapList(store.getOpeningHours(), OpeningHourDto::from))
                .regularHolidays(mapList(store.getRegularHolidays(), RegularHolidayDto::from))
                .temporaryHolidays(mapList(store.getTemporaryHolidays(), TemporaryHolidayDto::from))
                .images(mapList(store.getImages(), ImageDto::from))
                .menuCategories(mapList(store.getMenuCategories(), MenuCategoryDto::from))
                .isKept(isKept)
                .build();
    }

    private static <T, R> List<R> mapList(List<T> list, Function<T, R> mapper) {
        return list.stream().map(mapper).toList();
    }

    public record OpeningHourDto(Long id, String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        public static OpeningHourDto from(OpeningHour o) {
            return new OpeningHourDto(o.getId(), o.getDayOfWeek().name(), o.getStartTime(), o.getEndTime());
        }
    }

    public record RegularHolidayDto(Long id, String dayOfWeek, Integer weekInfo) {
        public static RegularHolidayDto from(RegularHoliday r) {
            return new RegularHolidayDto(r.getId(), r.getDayOfWeek().name(), r.getWeekInfo());
        }
    }

    public record TemporaryHolidayDto(Long id, String startDate, String endDate) {
        public static TemporaryHolidayDto from(TemporaryHoliday t) {
            return new TemporaryHolidayDto(t.getId(), t.getStartDate().toString(), t.getEndDate().toString());
        }
    }

    public record ImageDto(Long id, String url, boolean isMain) {
        public static ImageDto from(StoreImage i) {
            return new ImageDto(i.getId(), i.getImageUrl(), i.isMain());
        }
    }

    public record MenuCategoryDto(Long id, String name, List<MenuDto> menus) {
        public static MenuCategoryDto from(MenuCategory mc) {
            return new MenuCategoryDto(
                    mc.getId(),
                    mc.getName(),
                    mapList(mc.getMenus(), MenuDto::from)
            );
        }
    }

    public record MenuDto(Long id, String name, Integer price, String imageUrl) {
        public static MenuDto from(Menu m) {
            return new MenuDto(m.getId(), m.getName(), m.getPrice(), m.getImageUrl());
        }
    }
}
