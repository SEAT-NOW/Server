package depth.main.seatnow.global.exception.custom;

import depth.main.seatnow.global.exception.error.ErrorCode;

public class InternalServerException extends CustomException{
    public InternalServerException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InternalServerException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

}
