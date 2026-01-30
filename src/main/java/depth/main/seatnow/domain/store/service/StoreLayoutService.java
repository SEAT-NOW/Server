package depth.main.seatnow.domain.store.service;

import depth.main.seatnow.domain.store.dto.request.update.SpaceUpdateRequest;
import depth.main.seatnow.domain.store.entity.seat.Space;
import depth.main.seatnow.domain.store.entity.seat.TableConfig;
import depth.main.seatnow.domain.store.entity.store.Store;
import depth.main.seatnow.domain.store.repository.MenuCategoryRepository;
import depth.main.seatnow.domain.store.repository.MenuRepository;
import depth.main.seatnow.domain.store.repository.StoreRepository;
import depth.main.seatnow.domain.user.repository.UserRepository;
import depth.main.seatnow.global.exception.custom.NotFoundException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import depth.main.seatnow.infrastructure.external.s3.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static depth.main.seatnow.global.exception.error.ErrorCode.SPACE_NOT_FOUND;
import static depth.main.seatnow.global.exception.error.ErrorCode.STORE_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreLayoutService {
    private final StoreRepository storeRepository;

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

        store.updateTotalSeatCount();
        store.updateUsedSeatCount();
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
}
