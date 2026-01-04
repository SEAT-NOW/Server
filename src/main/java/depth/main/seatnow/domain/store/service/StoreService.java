package depth.main.seatnow.domain.store.service;

import depth.main.seatnow.domain.store.dto.request.OperationRequest;
import depth.main.seatnow.domain.store.dto.request.OwnerSignupRequest;
import depth.main.seatnow.domain.store.dto.request.SpaceRequest;
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
import depth.main.seatnow.global.exception.custom.ConflictException;
import depth.main.seatnow.infrastructure.external.s3.S3UploadService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static depth.main.seatnow.global.exception.error.ErrorCode.DUPLICATE_BUSINESS_NUMBER;
import static depth.main.seatnow.global.exception.error.ErrorCode.DUPLICATE_EMAIL;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final S3UploadService s3UploadService;
    private final PasswordEncoder passwordEncoder;

    public void registerOwner(OwnerSignupRequest request, MultipartFile licenseImage, List<MultipartFile> storeImages) {
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
        String licenseUrl = s3UploadService.uploadFileToPath(licenseImage, "permanent/license");

        Store store = Store.builder()
                .user(user)
                .representativeName(request.getBusiness().getRepresentativeName())
                .businessNumber(request.getBusiness().getBusinessNumber())
                .storeName(request.getBusiness().getStoreName())
                .address(request.getBusiness().getAddress())
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
        uploadAndMapImages(storeImages, store);

        // 가게 저장
        storeRepository.save(store);
    }

    private void uploadAndMapImages(List<MultipartFile> storeImages, Store store) {
        if (storeImages == null || storeImages.isEmpty()) return;

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
