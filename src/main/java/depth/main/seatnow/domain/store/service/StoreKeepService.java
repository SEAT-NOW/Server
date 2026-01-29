package depth.main.seatnow.domain.store.service;

import depth.main.seatnow.domain.store.dto.response.KeptStoreListResponse;
import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.entity.store.StoreKeep;
import depth.main.seatnow.domain.store.repository.StoreKeepRepository;
import depth.main.seatnow.domain.store.repository.StoreRepository;
import depth.main.seatnow.domain.user.entity.User;
import depth.main.seatnow.domain.user.repository.UserRepository;
import depth.main.seatnow.global.exception.custom.NotFoundException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreKeepService {

    private final StoreKeepRepository storeKeepRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    // 매장 킵하기(킵이 안돼 있으면 킵 or 킵 취소)
    @Transactional
    public boolean keepStore(Long userId, Long storeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        Optional<StoreKeep> storeKeep = storeKeepRepository.findByUserAndStore(user, store);

        if (storeKeep.isPresent()) {
            storeKeepRepository.delete(storeKeep.get());
            return false;
        } else {
            storeKeepRepository.save(StoreKeep.create(user, store));
            return true;
        }
    }

    // 킵한 매장 모두 반환
    public List<KeptStoreListResponse> getKeptStores(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        List<StoreKeep> keptStores = storeKeepRepository.findAllByUser(user);
        List<KeptStoreListResponse> responseList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (StoreKeep keep : keptStores) {
            Store store = keep.getStore();

            store.updateOperationStatus(now);//반환 전 업데이트 해주기

            responseList.add(KeptStoreListResponse.from(store));
        }

        return responseList;

    }
}
