package depth.main.seatnow.global.exception.custom;

import depth.main.seatnow.global.exception.error.ErrorCode;

public class UnauthorizedException extends CustomException{
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
