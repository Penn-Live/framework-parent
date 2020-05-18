package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

@Getter
@Setter
public class RelayRequest {

    private String relayUrl;
    private JSONObject bodyJson;
    private HttpMethod httpMethod;

}
