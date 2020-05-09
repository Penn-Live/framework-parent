package io.github.penn.rest;

import io.github.penn.rest.exception.RestCallException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Rest Response
 *
 * @author tangzhongping
 */
@Getter
@Setter
@ToString
public class RestResponse<T> {

    /**
     * if the call rpc exception
     */
    private Boolean ifCallException = false;

    T response;

    private RestCallException exception;


}


