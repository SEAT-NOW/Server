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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;
import java.util.function.Function;

@Getter
@Builder
@Schema(description = "매장 상세 정보 응답")
public class StoreDetailResponse {

    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "매장 이름", example = "명지대 꿈에도")
    private String storeName; // 매장 이름

    @Schema(description = "매장 주소", example = "경기도 용인시 처인구 명지로 40번길 10")
    private String address; // 주소

    @Schema(description = "읍면동 단위 지역명", example = "역북동")
    private String neighborhood; // 동,읍

    @Schema(description = "연관 대학교 목록", example = "[\"명지대학교\", \"용인대학교\"]")
    private List<String> universityNames; // 대학 이름

    @Schema(description = "매장 전화번호", example = "021234567")
    private String storePhone; // 매장 전화

    @Schema(description = "총 좌석 수", example = "30")
    private Integer totalSeatCount; // 전체 좌석 수

    @Schema(description = "현재 사용 중인 좌석 수", example = "13")
    private Integer usedSeatCount; // 사용중인 좌석 수

    @Schema(description = "매장 상태 태그 이름 (한글)", example = "혼잡")
    private SeatStatus statusTagName; // 혼잡도 상태

    @Schema(description = "현재 영업 상태 (영업 중, 곧 영업 종료, 영업 종료)", example = "영업 중")
    private OperationStatus operationStatus; // 운영 정보

    @Schema(description = "영업 시간 정보")
    private List<OpeningHourDto> openingHours; // 운영 시간

    @Schema(description = "정기 휴무 정보")
    private List<RegularHolidayDto> regularHolidays; // 휴일

    @Schema(description = "임시 휴무 정보")
    private List<TemporaryHolidayDto> temporaryHolidays; // 임시 휴일

    @Schema(description = "매장 이미지 목록(전부 다)")
    private List<ImageDto> images; // 가게 사진

    @Schema(description = "메뉴 관련 정보")
    private List<MenuCategoryDto> menuCategories; // 메뉴 정보

    @Schema(description = "즐겨찾기 여부", example = "true")
    private boolean isKept; // 즐겨찾기 유무

    public static StoreDetailResponse from(Store store, boolean isKept) {
        return StoreDetailResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .neighborhood(store.getNeighborhood())
                .universityNames(store.getStoreUniversities().stream()
                        .map(su -> su.getUniversityMaster().getName())
                        .toList())
                .storePhone(store.getStorePhone())
                .totalSeatCount(store.getTotalSeatCount())
                .usedSeatCount(store.getUsedSeatCount())
                .statusTagName(store.getStatusTag())
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

    private record OpeningHourDto(Long id, String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        private static OpeningHourDto from(OpeningHour o) {
            return new OpeningHourDto(o.getId(), o.getDayOfWeek().name(), o.getStartTime(), o.getEndTime());
        }
    }

    private record RegularHolidayDto(Long id, String dayOfWeek, Integer weekInfo) {
        private static RegularHolidayDto from(RegularHoliday r) {
            return new RegularHolidayDto(r.getId(), r.getDayOfWeek().name(), r.getWeekInfo());
        }
    }

    private record TemporaryHolidayDto(Long id, String startDate, String endDate) {
        private static TemporaryHolidayDto from(TemporaryHoliday t) {
            return new TemporaryHolidayDto(t.getId(), t.getStartDate().toString(), t.getEndDate().toString());
        }
    }

    private record ImageDto(Long id, String url, boolean isMain) {
        private static ImageDto from(StoreImage i) {
            return new ImageDto(i.getId(), i.getImageUrl(), i.isMain());
        }
    }

    private record MenuCategoryDto(Long id, String name, List<MenuDto> menus) {
        private static MenuCategoryDto from(MenuCategory mc) {
            return new MenuCategoryDto(
                    mc.getId(),
                    mc.getName(),
                    mapList(mc.getMenus(), MenuDto::from)
            );
        }
    }

    private record MenuDto(Long id, String name, Integer price, String imageUrl) {
        private static MenuDto from(Menu m) {
            return new MenuDto(m.getId(), m.getName(), m.getPrice(), m.getImageUrl());
        }
    }
}
