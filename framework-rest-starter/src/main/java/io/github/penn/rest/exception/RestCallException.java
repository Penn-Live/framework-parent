package io.github.penn.rest.exception;

/**
 * 表示REST调用过程中的异常
 *
 * @author tangzhongping
 * @date:2020/5/8
 */
public class RestCallException extends RuntimeException {

    private static long serialVersionID = 1564897894316579464L;

    public RestCallException() {
    }

    public RestCallException(String message) {
        super(message);
    }

    public RestCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestCallException(Throwable cause) {
        super(cause);
    }

    public RestCallException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
