package depth.main.seatnow.domain.store.service;

import depth.main.seatnow.domain.store.dto.request.update.OperationUpdateRequest;
import depth.main.seatnow.domain.store.dto.request.update.StorePhotoUpdateRequest;
import depth.main.seatnow.domain.store.dto.response.OperationInfoResponse;
import depth.main.seatnow.domain.store.entity.operation.OpeningHour;
import depth.main.seatnow.domain.store.entity.operation.RegularHoliday;
import depth.main.seatnow.domain.store.entity.operation.TemporaryHoliday;
import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.entity.store.StoreImage;
import depth.main.seatnow.domain.store.repository.StoreRepository;
import depth.main.seatnow.global.exception.custom.NotFoundException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import depth.main.seatnow.infrastructure.external.s3.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreOperationService {
    private final StoreRepository storeRepository;
    private final S3UploadService s3UploadService;

    @Transactional
    public void updateStorePhone(Long userId, String storePhone) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        // 가게 연락처 업데이트
        store.updateStorePhone(storePhone);
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

    @Transactional
    public void updateStoreImages(Long userId, StorePhotoUpdateRequest request, List<MultipartFile> newImages) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        List<StoreImage> currentImages = store.getImages();

        // 1. 삭제: 요청 리스트에 없는 기존 이미지는 S3와 DB에서 제거
        List<Long> keepIds = request.getExistingImages().stream()
                .map(StorePhotoUpdateRequest.ExistingImageDto::getId)
                .toList();

        List<StoreImage> toDelete = currentImages.stream()
                .filter(img -> !keepIds.contains(img.getId()))
                .toList();

        toDelete.forEach(img -> s3UploadService.deleteFile(img.getImageUrl()));
        currentImages.removeAll(toDelete);

        // 2. 수정: 기존 이미지 중 대표 설정값이 변한 경우만 업데이트
        for (StorePhotoUpdateRequest.ExistingImageDto dto : request.getExistingImages()) {
            currentImages.stream()
                    .filter(img -> img.getId().equals(dto.getId()))
                    .findFirst()
                    .ifPresent(img -> {
                        if (img.isMain() != dto.isMain()) {
                            img.updateMain(dto.isMain());
                        }
                    });
        }

        // 3. 현재 살아남은 사진들 중 대표 사진이 있는지 체크
        boolean hasMainImage = currentImages.stream()
                .anyMatch(StoreImage::isMain);

        // 4. 추가: 신규 사진 업로드 및 대표 우선순위 적용
        if (newImages != null && !newImages.isEmpty()) {
            for (int i = 0; i < newImages.size(); i++) {
                MultipartFile file = newImages.get(i);

                if (file != null && !file.isEmpty()) {
                    String url = s3UploadService.uploadFileToPath(file, "permanent/store");
                    boolean shouldBeMain = (!hasMainImage && i == 0);
                    currentImages.add(StoreImage.create(url, shouldBeMain, store));
                    if (shouldBeMain) {
                        hasMainImage = true;
                    }
                }
            }
        }
    }

    public OperationInfoResponse getOperationInfo(Long userId) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        return OperationInfoResponse.of(store);
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
}
