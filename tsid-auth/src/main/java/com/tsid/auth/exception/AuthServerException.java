package com.tsid.auth.exception;

public class AuthServerException extends RuntimeException{

    private static final long serialVersionUID = 1L;
    private ErrCode errCode;

    public AuthServerException(ErrCode errCode) {
        this.errCode = errCode;
    }

    public AuthServerException(ErrCode errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public AuthServerException(ErrCode errCode, Throwable cause) {
        super(cause);
        this.errCode = errCode;
    }

    public AuthServerException(ErrCode errCode, String message, Throwable cause) {
        super(message, cause);
        this.errCode = errCode;
    }

    public ErrCode getErrCode(){
        return this.errCode;
    }
}
