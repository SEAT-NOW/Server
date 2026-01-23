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
public class StoreResponse {

    private Long storeId;

    private String storeName;

    private String address;

    private String neighborhood;

    private List<String> universityNames;

    private String storePhone;

    private Integer totalSeatCount;
    private Integer usedSeatCount;
    private SeatStatus statusTag;

    private OperationStatus operationStatus;

    private List<OpeningHourDto> openingHours;
    private List<RegularHolidayDto> regularHolidays;
    private List<TemporaryHolidayDto> temporaryHolidays;

    private List<ImageDto> images;
    private List<MenuCategoryDto> menuCategories;

    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
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
