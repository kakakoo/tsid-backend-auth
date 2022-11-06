package com.tsid.auth.exception;

public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 99L;
    private ErrCode errCode;

    public CustomException(ErrCode errCode) {
        this.errCode = errCode;
    }

    public CustomException(ErrCode errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public CustomException(ErrCode errCode, Throwable cause) {
        super(cause);
        this.errCode = errCode;
    }

    public CustomException(ErrCode errCode, String message, Throwable cause) {
        super(message, cause);
        this.errCode = errCode;
    }

    public ErrCode getErrCode(){
        return this.errCode;
    }
}
