package depth.main.seatnow.domain.store.service;

import depth.main.seatnow.domain.store.dto.request.OperationRequest;
import depth.main.seatnow.domain.store.dto.request.OwnerSignupRequest;
import depth.main.seatnow.domain.store.dto.request.OwnerWithdrawRequest;
import depth.main.seatnow.domain.store.dto.request.SpaceRequest;
import depth.main.seatnow.domain.store.dto.response.SeatResponse;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static depth.main.seatnow.global.exception.error.ErrorCode.*;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
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

}
