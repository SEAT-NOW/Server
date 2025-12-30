package depth.main.seatnow.domain.store.service;

import depth.main.seatnow.domain.store.dto.request.ImageRequest;
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
import org.springframework.stereotype.Service;

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

    public void registerOwner(OwnerSignupRequest request) {
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
                .password(request.getAccount().getPassword())
                .phoneNumber(request.getAccount().getPhoneNumber())
                .role(Role.OWNER)
                .build();
        userRepository.save(user);

        // 사업자 정보 처리
        String finalLicense = s3UploadService.confirmImage(request.getBusiness().getBusinessLicenseUrl());

        Store store = Store.builder()
                .user(user)
                .representativeName(request.getBusiness().getRepresentativeName())
                .businessNumber(request.getBusiness().getBusinessNumber())
                .storeName(request.getBusiness().getStoreName())
                .address(request.getBusiness().getAddress())
                .universityNames(request.getBusiness().getUniversityNames())
                .storePhone(request.getBusiness().getStorePhone())
                .businessLicenseUrl(finalLicense)
                .build();

        // 부가 정보 매핑
        mapOperations(request.getOperation(), store);
        mapLayouts(request.getLayout(), store);
        mapImages(request.getImages(), store);

        // 가게 저장
        storeRepository.save(store);
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
        layout.forEach(spaceDto -> {
            Space space = Space.create(spaceDto.getName(), store);

            spaceDto.getTables().forEach(tableDto ->
                    space.getTableConfigs().add(
                            TableConfig.create(tableDto.getTableType(), tableDto.getTableCount(), space)
                    )
            );
            store.getSpaces().add(space);
        });
    }

    private void mapImages(List<ImageRequest> images, Store store) {
        images.forEach(imgDto -> {
            String permanentUrl = s3UploadService.confirmImage(imgDto.getImageUrl());

            store.getImages().add(
                    StoreImage.create(permanentUrl, imgDto.getIsPrimary(), store)
            );
        });
    }
}
