package depth.main.seatnow.domain.store.service;

import depth.main.seatnow.domain.store.dto.request.SpaceSeatUpdateRequest;
import depth.main.seatnow.domain.store.dto.response.SpaceSeatUpdateResponse;
import depth.main.seatnow.domain.store.entity.seat.Space;
import depth.main.seatnow.domain.store.entity.seat.TableConfig;
import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.repository.StoreRepository;
import depth.main.seatnow.domain.store.repository.TableConfigRepository;
import depth.main.seatnow.global.exception.custom.BadRequestException;
import depth.main.seatnow.global.exception.custom.ForbiddenException;
import depth.main.seatnow.global.exception.custom.NotFoundException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import depth.main.seatnow.global.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class SeatService {
    private final StoreRepository storeRepository;
    private final TableConfigRepository tableConfigRepository;

    @Transactional
    public SpaceSeatUpdateResponse updateAllSeats(CustomUserDetails userDetails, SpaceSeatUpdateRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND));

        // 매장의 주인이 로그인한 사장님이 맞는지 확인!
        if (!store.getUser().getId().equals(userDetails.getUserId())) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        // 테이블 수정 후 전체 합산
        for (var spaceUpdate : request.getSpaceUpdates()) {
            boolean isSpaceOwnedByStore = false; // 매장에 그 공간이 존재하는지 여부
            for (Space s : store.getSpaces()) {
                if (s.getId().equals(spaceUpdate.getSpaceId())) {
                    isSpaceOwnedByStore = true;
                    break;
                }
            }

            if (!isSpaceOwnedByStore) {
                throw new NotFoundException(ErrorCode.SPACE_NOT_FOUND);
            }

            for (var tableUpdate : spaceUpdate.getTableUpdates()) {
                // 1. DB에 해당 테이블이 존재하는지 먼저 찾기
                TableConfig table = tableConfigRepository.findById(tableUpdate.getTableConfigId())
                        .orElseThrow(() -> new NotFoundException(ErrorCode.TABLE_NOT_FOUND));

                // 2. 찾은 테이블이 속한 공간이 요청에서의 공간ID랑 같은지 확인
                if (!table.getSpace().getId().equals(spaceUpdate.getSpaceId())) {
                    throw new ForbiddenException(ErrorCode.FORBIDDEN);
                }

                if (tableUpdate.getUsedCount() > table.getTableCount()) {
                    throw new BadRequestException(ErrorCode.INVALID_TABLE_COUNT);
                }

                table.updateUsedCount(tableUpdate.getUsedCount());
            }
        }

        // 합산 및 태그 업데이트
        int totalUsedSeats = 0;
        for (Space space : store.getSpaces()) {
            for (TableConfig table : space.getTableConfigs()) {
                totalUsedSeats += (table.getUsedCount() * table.getTableType());
            }
        }

        store.updateUsedSeatCount(totalUsedSeats);
        store.updateStatusTag();

        return SpaceSeatUpdateResponse.from(store);
    }
}
