package depth.main.seatnow.domain.store.service;

import depth.main.seatnow.domain.store.dto.response.KeptStoreListResponse;
import depth.main.seatnow.domain.store.dto.response.StoreListResponse;
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

    public List<KeptStoreListResponse> getKeptStores(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        List<StoreKeep> keptStores = storeKeepRepository.findAllByUser(user);
        List<KeptStoreListResponse> responseList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (StoreKeep keep : keptStores) {
            Store store = keep.getStore();

            store.updateOperationStatus(now);

            responseList.add(KeptStoreListResponse.from(store));
        }

        return responseList;

    }
}
