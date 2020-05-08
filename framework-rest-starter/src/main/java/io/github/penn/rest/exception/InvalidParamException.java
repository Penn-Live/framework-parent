package io.github.penn.rest.exception;

/**
 * 表示参数的一些异常
 * @author tangzhongping
 * @date:2020/5/8
 */
public class InvalidParamException extends RuntimeException{

    private static  long serialVersionID=1564897894316579465L;


    public InvalidParamException() {
    }

    public InvalidParamException(String message) {
        super(message);
    }

    public InvalidParamException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidParamException(Throwable cause) {
        super(cause);
    }

    public InvalidParamException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
