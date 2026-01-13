package depth.main.seatnow.domain.store.entity.operation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperationStatus {

    OPEN("영업중"),
    CLOSING_SOON("곧 영업 종료"),
    CLOSED("영업 종료");

    private final String description;
}
