package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.penn.rest.exception.RestCallException;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Rest Response
 *
 * @author tangzhongping
 */
@Getter
@Setter
@JsonIgnoreProperties("exception")
public class RestResponse<T> implements Serializable {

    private static final long serialVersionUID = -3042686055658047285L;

    /**
     * if the call rpc exception
     */
    private Boolean ifCallException = false;


    /**
     * if bis Exception
     */
    private Boolean ifBisException = false;


    T response;

    private RestCallException exception;

    @Override
    public String toString() {
        return "RestResponse{" +
                "ifCallException=" + ifCallException +
                ", response=" + response +
                ", exception=" + (exception == null ? "null" : exception.getLocalizedMessage()) +
                '}';
    }


    /**
     * throw if call fail
     */
    public void throwExceptionIfCallFail() {
        if (ifCallException) {
            throw exception;
        }
    }


    /**
     * throw if call fail
     */
    public void throwExceptionIfCallFail(JSONObject jsonObject) {
        if (ifCallException) {
            exception.setExceptionDesc(jsonObject);
            throw exception;
        }
    }


}


