package depth.main.seatnow.domain.store.service;

import depth.main.seatnow.domain.store.dto.request.*;
import depth.main.seatnow.domain.store.dto.response.SeatResponse;
import depth.main.seatnow.domain.store.dto.response.StoreListResponse;
import depth.main.seatnow.domain.store.entity.operation.OpeningHour;
import depth.main.seatnow.domain.store.entity.operation.RegularHoliday;
import depth.main.seatnow.domain.store.entity.operation.TemporaryHoliday;
import depth.main.seatnow.domain.store.entity.seat.Space;
import depth.main.seatnow.domain.store.entity.seat.TableConfig;
import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.entity.store.StoreImage;
import depth.main.seatnow.domain.store.repository.StoreRepository;
import depth.main.seatnow.domain.user.entity.User;
import depth.main.seatnow.domain.user.entity.enums.Role;
import depth.main.seatnow.domain.user.repository.UserRepository;
import depth.main.seatnow.global.exception.custom.BadRequestException;
import depth.main.seatnow.global.exception.custom.ConflictException;
import depth.main.seatnow.global.exception.custom.ForbiddenException;
import depth.main.seatnow.global.exception.custom.NotFoundException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import depth.main.seatnow.global.security.CustomUserDetails;
import depth.main.seatnow.infrastructure.external.s3.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static depth.main.seatnow.global.exception.error.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final S3UploadService s3UploadService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long registerOwner(OwnerSignupRequest request, MultipartFile licenseImage, List<MultipartFile> storeImages) {
        // 계정 중복 검증
        if (userRepository.existsByEmail(request.getAccount().getEmail())) {
            throw new ConflictException(DUPLICATE_EMAIL);
        }

        // 사업자 번호 중복 검증
        if (storeRepository.existsByBusinessNumber(request.getBusiness().getBusinessNumber())) {
            throw new ConflictException(DUPLICATE_BUSINESS_NUMBER);
        }

        // 유저 저장
        User user = User.builder()
                .email(request.getAccount().getEmail())
                .password(passwordEncoder.encode(request.getAccount().getPassword()))
                .phoneNumber(request.getAccount().getPhoneNumber())
                .role(Role.OWNER)
                .build();
        userRepository.save(user);

        // 사업자 등록증 S3 업로드
        String licenseUrl = null;
        if (licenseImage != null && !licenseImage.isEmpty()) {
            licenseUrl = s3UploadService.uploadFileToPath(licenseImage, "permanent/license");
        }

        Store store = Store.builder()
                .user(user)
                .representativeName(request.getBusiness().getRepresentativeName())
                .businessNumber(request.getBusiness().getBusinessNumber())
                .storeName(request.getBusiness().getStoreName())
                .address(request.getBusiness().getAddress())
                .neighborhood(request.getBusiness().getNeighborhood())
                .latitude(request.getBusiness().getLatitude())
                .longitude(request.getBusiness().getLongitude())
                .universityNames(request.getBusiness().getUniversityNames())
                .storePhone(request.getBusiness().getStorePhone())
                .businessLicenseUrl(licenseUrl)
                .build();

        // 부가 정보 매핑
        mapOperations(request.getOperation(), store);
        mapLayouts(request.getLayout(), store);

        // 매장 이미지 일괄 업로드 및 매핑
        if (storeImages != null && !storeImages.isEmpty()) {
            uploadAndMapImages(storeImages, store);
        }

        // 가게 저장
        Store saveStore = storeRepository.save(store);

        return saveStore.getId();
    }

    @Transactional
    public SeatResponse getStoreSeatStatus(Long storeId, CustomUserDetails userDetails) {
        // 매장 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        // 매장의 주인이 로그인한 사장님이 맞는지 확인!
        if (!store.getUser().getId().equals(userDetails.getUserId())) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        return SeatResponse.from(store);
    }

    @Transactional
    public void withdrawOwner(CustomUserDetails userDetails, OwnerWithdrawRequest request) {
        User owner = userDetails.getUser();

        // 매장 조회
        Store store = storeRepository.findByUserId(owner.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), owner.getPassword())) {
            throw new BadRequestException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 사업자 등록번호 검증
        if (!store.getBusinessNumber().equals(request.getBusinessNumber())) {
            throw new BadRequestException(ErrorCode.INVALID_BUSINESS_NUMBER);
        }

        // 삭제
        storeRepository.delete(store);
        userRepository.delete(owner);
    }

    @Transactional
    public List<StoreListResponse> searchStores(String keyword, Double lat, Double lng, Double radius, Integer headCount) {
        List<Store> stores;

        // 목록 조회
        if (keyword != null && !keyword.isBlank()) {
            // 키워드 검색
            stores = storeRepository.searchByKeyword(keyword);
        } else if (lat != null && lng != null) {
            // 키워드가 없을 때만 위치 기반 반경 검색
            stores = storeRepository.searchByLocation(lat, lng, radius);
        } else {
            return List.of();
        }

        // 인원수 필터링
        // headCount가 1 이상일 때만 남은 좌석 수 비교
        if (headCount != null && headCount > 0) {
            List<Store> filteredStores = new ArrayList<>();
            for (Store store : stores) {
                if (store.getAvailableSeatCount() >= headCount) {
                    filteredStores.add(store);
                }
            }
            stores = filteredStores;
        }

        List<StoreListResponse> responseList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Store store : stores) {
            String distanceStr = null;
            store.updateOperationStatus(now);

            if (lat != null && lng != null) {
                double distanceMeters = calculateDistance(lat, lng, store.getLatitude(), store.getLongitude());
                distanceStr = formatDistance(distanceMeters);
            }

            responseList.add(StoreListResponse.from(store, distanceStr));
        }

        return responseList;
    }

    public void verifyPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException(ErrorCode.PASSWORD_MISMATCH);
        }
    }

    @Transactional
    public void updatePassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        user.updatePassword(passwordEncoder.encode(password));
    }

    @Transactional
    public void updateStorePhone(Long userId, String storePhone) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        // 가게 연락처 업데이트
        store.updateStorePhone(storePhone);
    }
    @Transactional
    public void updateSpaces(Long userId, List<SpaceUpdateRequest> request) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(STORE_NOT_FOUND));

        // 기존 공간(Space) 가져오기
        List<Space> existingSpaces = store.getSpaces();

        // 요청 데이터 Id들을 모아서 삭제될 대상 판별
        List<Long> requestSpaceIds = request.stream()
                .map(SpaceUpdateRequest::getId)
                .filter(id -> id != null)
                .toList();

        // 요청에 없는 기존 공간 삭제
        existingSpaces.removeIf(space -> !requestSpaceIds.contains(space.getId()));

        // 공간 수정 및 추가
        for(SpaceUpdateRequest spaceUpdateRequest : request){
            if(spaceUpdateRequest.getId() != null){
                Space space = existingSpaces.stream()
                        .filter(s -> s.getId().equals(spaceUpdateRequest.getId()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException(SPACE_NOT_FOUND));

                space.updateName(spaceUpdateRequest.getName());
                updateTableConfigs(space, spaceUpdateRequest.getTables());
            } else{
                // 신규 공간 생성
                Space newSpace = Space.create(spaceUpdateRequest.getName(), store);
                existingSpaces.add(newSpace);

                spaceUpdateRequest.getTables().forEach(tableDto ->{
                        TableConfig newTable = TableConfig.create(tableDto.getTableType(), tableDto.getTableCount(), newSpace);
                        newSpace.getTableConfigs().add(newTable);
                });

            }
        }

    }

    @Transactional
    public void updateOperationInfo(Long userId, OperationUpdateRequest request) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        // 1. 정기 휴무 업데이트
        updateRegularHolidays(store, request.getRegularHolidays());
        // 2. 임시 휴무 업데이트
        updateTemporaryHolidays(store, request.getTemporaryHolidays());
        // 3. 영업 시간 업데이트
        updateOpeningHours(store, request.getHours());
    }

    private void updateOpeningHours(Store store, List<OperationUpdateRequest.OpeningHourUpdateDto> hours) {
        List<OpeningHour> existing = store.getOpeningHours();
        List<Long> requestIds = hours.stream()
                .map(OperationUpdateRequest.OpeningHourUpdateDto::getId)
                .filter(Objects::nonNull)
                .toList();

        existing.removeIf(h -> !requestIds.contains(h.getId()));

        for (OperationUpdateRequest.OpeningHourUpdateDto dto : hours) {
            if (dto.getId() != null) {
                OpeningHour hour = existing.stream()
                        .filter(h -> h.getId().equals(dto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException(ErrorCode.OPENING_HOUR_NOT_FOUND));
                hour.update(dto.getDayOfWeek(), dto.getStartTime(), dto.getEndTime());
            } else {
                existing.add(OpeningHour.create(dto.getDayOfWeek(), dto.getStartTime(), dto.getEndTime(), store));
            }
        }
    }

    private void updateTemporaryHolidays(Store store, List<OperationUpdateRequest.TemporaryHolidayUpdateDto> temporaryHolidays) {
        List<TemporaryHoliday> existing = store.getTemporaryHolidays();
        List<Long> requestIds = temporaryHolidays.stream()
                .map(OperationUpdateRequest.TemporaryHolidayUpdateDto::getId)
                .filter(Objects::nonNull)
                .toList();

        existing.removeIf(h -> !requestIds.contains(h.getId()));

        for (OperationUpdateRequest.TemporaryHolidayUpdateDto dto : temporaryHolidays) {
            if (dto.getId() != null) {
                TemporaryHoliday holiday = existing.stream()
                        .filter(h -> h.getId().equals(dto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException(ErrorCode.HOLIDAY_NOT_FOUND));
                holiday.update(dto.getStartDate(), dto.getEndDate());
            } else {
                existing.add(TemporaryHoliday.create(dto.getStartDate(), dto.getEndDate(), store));
            }
        }
    }

    private void updateRegularHolidays(Store store, List<OperationUpdateRequest.RegularHolidayUpdateDto> regularHolidays) {
        List<RegularHoliday> existing = store.getRegularHolidays();
        List<Long> requestIds = regularHolidays.stream()
                .map(OperationUpdateRequest.RegularHolidayUpdateDto::getId)
                .filter(Objects::nonNull)
                .toList();

        existing.removeIf(h -> !requestIds.contains(h.getId())); // 삭제

        for (OperationUpdateRequest.RegularHolidayUpdateDto dto : regularHolidays) {
            if (dto.getId() != null) { // 수정
                RegularHoliday holiday = existing.stream()
                        .filter(h -> h.getId().equals(dto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException(ErrorCode.HOLIDAY_NOT_FOUND));
                holiday.update(dto.getDayOfWeek(), dto.getWeekInfo());
            } else {
                existing.add(RegularHoliday.create(dto.getDayOfWeek(), dto.getWeekInfo(), store));
            }
        }
    }

    private void updateTableConfigs(Space space, List<SpaceUpdateRequest.TableUpdateDto> tables) {
        List<TableConfig> existingTables = space.getTableConfigs();

        // 요청에 없는 테이블 ID 삭제
        List<Long> requestTableIds = tables.stream()
                .map(SpaceUpdateRequest.TableUpdateDto::getId)
                .filter(id -> id != null)
                .toList();

        existingTables.removeIf(table -> !requestTableIds.contains(table.getId()));

        // 수정 및 추가
        for (SpaceUpdateRequest.TableUpdateDto tableDto : tables) {
            if (tableDto.getId() != null) {
                // 기존 테이블 설정 업데이트
                TableConfig table = existingTables.stream()
                        .filter(t -> t.getId().equals(tableDto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException(ErrorCode.TABLE_NOT_FOUND));

                table.updateConfig(tableDto.getTableType(), tableDto.getTableCount());
            } else {
                // 신규 테이블 설정 생성
                TableConfig newTable = TableConfig.create(
                        tableDto.getTableType(),
                        tableDto.getTableCount(),
                        space
                );
                existingTables.add(newTable);
            }
        }

    }

    private void uploadAndMapImages(List<MultipartFile> storeImages, Store store) {
        for (int i = 0; i < storeImages.size(); i++) {
            MultipartFile imgFile = storeImages.get(i);
            String imageUrl = s3UploadService.uploadFileToPath(imgFile, "permanent/store");

            // 첫 번째 이미지를 기본(Primary) 이미지로 설정
            boolean isPrimary = (i == 0);

            store.getImages().add(
                    StoreImage.create(imageUrl, isPrimary, store)
            );
        }
    }

    private void mapOperations(OperationRequest request, Store store) {
        request.getHours().forEach(h ->
                store.getOpeningHours().add(
                        OpeningHour.create(h.getDayOfWeek(), h.getStartTime(), h.getEndTime(), store)
                )
        );
        request.getRegularHolidays().forEach(rh ->
                store.getRegularHolidays().add(
                        RegularHoliday.create(rh.getDayOfWeek(), rh.getWeekInfo(), store)
                )
        );

        request.getTemporaryHolidays().forEach(th ->
                store.getTemporaryHolidays().add(
                        TemporaryHoliday.create(th.getStartDate(), th.getEndDate(), store)
                )
        );

    }
    private void mapLayouts(List<SpaceRequest> layout, Store store) {
        int totalSeats = 0; // 전체 좌석 수를 담을 변수

        for (SpaceRequest spaceDto : layout) {
            Space space = Space.create(spaceDto.getName(), store);

            for (var tableDto : spaceDto.getTables()) {
                // TableConfig 생성
                TableConfig tableConfig = TableConfig.create(
                        tableDto.getTableType(),
                        tableDto.getTableCount(),
                        space
                );
                space.getTableConfigs().add(tableConfig);

                // 전체 좌석 수 합산: (테이블 인원수 * 테이블 개수)
                totalSeats += (tableDto.getTableType() * tableDto.getTableCount());
            }
            store.getSpaces().add(space);
        }

        // Store 엔티티에 합산된 결과 저장
        store.initializeSeatInfo(totalSeats);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구의 반지름 (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000; // km를 m로 변환
    }

    private String formatDistance(double meters) {
        if (meters < 1000) {
            return String.format("%.0fm", meters);
        } else {
            return String.format("%.1fkm", meters / 1000.0);
        }
    }


}
