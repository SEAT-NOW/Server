package depth.main.seatnow.global.exception.custom;

import depth.main.seatnow.global.exception.error.ErrorCode;
import lombok.Getter;

@Getter
public class ForbiddenException extends CustomException{
    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
