package depth.main.seatnow.global.exception.custom;

import depth.main.seatnow.global.exception.error.ErrorCode;

public class NotFoundException extends CustomException{
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
