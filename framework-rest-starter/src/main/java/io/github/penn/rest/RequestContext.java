package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpMethod;

/**
 * request context
 */
@Setter
@Getter
@ToString
public class RequestContext {

    /**
     * completedUrl
     */
    private String completedUrl;

    /**
     * params
     */
    private JSONObject params;

    /**
     * method
     */
    private HttpMethod httpMethod;


}
