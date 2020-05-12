package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

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

    /**
     * which rest Template to use
     */
    private RestTemplate restTemplate;


}
