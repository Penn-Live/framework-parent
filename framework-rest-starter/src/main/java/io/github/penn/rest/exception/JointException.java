package io.github.penn.rest.exception;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

/**
 * 表示聚合过程中的异常
 *
 * @author tangzhongping
 * @date:2020/5/8
 */
public class JointException extends RuntimeException {

    private static long serialVersionID = 1564897894316579464L;

    /**
     * exception desc that catch by advice
     */
    @Getter
    @Setter
    private JSONObject exceptionDesc;

    public JointException() {
    }

    public JointException(String message) {
        super(message);
    }

    public JointException(String message, Throwable cause) {
        super(message, cause);
    }

    public JointException(Throwable cause) {
        super(cause);
    }

    public JointException(String message, Throwable cause, boolean enableSuppression,
                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public JointException(String message, Throwable cause, JSONObject exceptionDesc) {
        this(message, cause);
        this.exceptionDesc = exceptionDesc;
    }
}
