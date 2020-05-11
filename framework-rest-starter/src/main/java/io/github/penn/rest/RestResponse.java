package io.github.penn.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.penn.rest.exception.RestCallException;
import lombok.Getter;
import lombok.Setter;

/**
 * Rest Response
 *
 * @author tangzhongping
 */
@Getter
@Setter
@JsonIgnoreProperties("exception")
public class RestResponse<T> {

  /**
   * if the call rpc exception
   */
  private Boolean ifCallException = false;

  T response;

  private RestCallException exception;

  @Override
  public String toString() {
    return "RestResponse{" +
        "ifCallException=" + ifCallException +
        ", response=" + response +
        ", exception=" + (exception==null?"null":exception.getLocalizedMessage()) +
        '}';
  }
}


