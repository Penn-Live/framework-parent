package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import io.github.penn.rest.exception.InvalidParamException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * exception advice
 */
@RestControllerAdvice
@Order(1)
@Slf4j
public class RestExceptionAdvice {


    /**
     * InvalidParamException
     */
    @ExceptionHandler({InvalidParamException.class})
    public JSONObject handleInvalidParamException(InvalidParamException invalidParamException) {
        log.error("invalid param exception ", invalidParamException);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("retCode", 99);
        jsonObject.put("retMsg", invalidParamException.getMessage());
        return jsonObject;
    }


}
