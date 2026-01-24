package depth.main.seatnow.domain.store.service;

import depth.main.seatnow.domain.store.dto.request.update.MenuCategoryUpdateRequest;
import depth.main.seatnow.domain.store.dto.request.update.MenuUpdateRequest;
import depth.main.seatnow.domain.store.dto.response.StoreMenuResponse;
import depth.main.seatnow.domain.store.entity.menu.Menu;
import depth.main.seatnow.domain.store.entity.menu.MenuCategory;
import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.repository.MenuCategoryRepository;
import depth.main.seatnow.domain.store.repository.MenuRepository;
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
public class StoreMenuService {
    private final StoreRepository storeRepository;
    private final S3UploadService s3UploadService;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public void updateMenuCategories(Long userId, MenuCategoryUpdateRequest request) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        List<MenuCategory> currentCategories = store.getMenuCategories();

        // 1. 삭제: 요청에 없는 기존 카테고리 식별 및 제거
        List<Long> keepIds = request.getCategories().stream()
                .map(MenuCategoryUpdateRequest.CategoryDto::getId)
                .filter(Objects::nonNull)
                .toList();

        currentCategories.removeIf(category -> !keepIds.contains(category.getId()));

        // 2. 수정 및 추가
        for (MenuCategoryUpdateRequest.CategoryDto dto : request.getCategories()) {
            if (dto.getId() != null) {
                // 수정: 기존 ID가 있으면 이름 업데이트
                MenuCategory category = currentCategories.stream()
                        .filter(cat -> cat.getId().equals(dto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

                category.updateName(dto.getName());
            } else {
                // 추가: ID가 없으면 신규 생성 후 리스트에 추가
                currentCategories.add(MenuCategory.builder()
                        .name(dto.getName())
                        .store(store)
                        .build());
            }
        }
    }

    @Transactional
    public void saveOrUpdateMenu(Long userId, MenuUpdateRequest request, MultipartFile menuImage) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        MenuCategory category = menuCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        if(request.getId() == null){
            // 신규 등록
            String imageUrl = null;
            if(menuImage != null && !menuImage.isEmpty()){
                imageUrl = s3UploadService.uploadFileToPath(menuImage, "permanent/menu");
            }

            Menu newMenu = Menu.builder()
                    .name(request.getName())
                    .price(request.getPrice())
                    .imageUrl(imageUrl)
                    .menuCategory(category)
                    .build();
            menuRepository.save(newMenu);
        }else {
            // 기존 수정
            Menu menu = menuRepository.findById(request.getId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.MENU_NOT_FOUND));

            // 사진 처리 로직
            String currentImageUrl = menu.getImageUrl();
            if(menuImage != null && !menuImage.isEmpty()){
                // 새로운 사진이 들어온 경우: 기존 S3 파일 삭제 후 업로드
                if(currentImageUrl != null){
                    s3UploadService.deleteFile(currentImageUrl);
                }
                currentImageUrl = s3UploadService.uploadFileToPath(menuImage,"permanent/menu");
            }else if(!request.isKeepImage() && currentImageUrl != null){
                s3UploadService.deleteFile(currentImageUrl);
                currentImageUrl = null;
            }

            menu.updateMenuDetails(request.getName(), request.getPrice(), currentImageUrl, category);
        }
    }

    public StoreMenuResponse getStoreMenus(Long userId) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        return StoreMenuResponse.of(store.getMenuCategories());
    }
}
