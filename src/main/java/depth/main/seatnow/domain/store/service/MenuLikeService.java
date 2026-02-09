package depth.main.seatnow.domain.store.service;

import depth.main.seatnow.domain.store.entity.menu.Menu;
import depth.main.seatnow.domain.store.entity.menu.MenuLike;
import depth.main.seatnow.domain.store.repository.MenuLikeRepository;
import depth.main.seatnow.domain.store.repository.MenuRepository;
import depth.main.seatnow.domain.user.entity.User;
import depth.main.seatnow.domain.user.repository.UserRepository;
import depth.main.seatnow.global.exception.custom.NotFoundException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuLikeService {

    private final MenuRepository menuRepository;
    private final MenuLikeRepository menuLikeRepository;
    private final UserRepository userRepository;

    // 메뉴 좋아요 누르기(눌렀으면 취소, 안눌렀으면 누르기)
    @Transactional
    public boolean toggleMenuLike(Long userId, Long menuId) {


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MENU_NOT_FOUND));

        Optional<MenuLike> existingLike = menuLikeRepository.findByUserAndMenu(user, menu);

        // 이미 좋아요를 눌렀는지 확인
        if (existingLike.isPresent()) { // 이미 좋아요가 있으면 삭제
            MenuLike menuLike = existingLike.get();
            menuLikeRepository.delete(menuLike);
            menu.decreaseLikeCount();

            return false;
        } else { // 좋아요 없으면 좋아요 누르기
            MenuLike newLike = MenuLike.builder().user(user).menu(menu).build();
            menuLikeRepository.save(newLike);
            menu.increaseLikeCount();

            return true;
        }
    }
}
