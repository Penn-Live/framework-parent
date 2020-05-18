package io.github.penn.rest;


import com.alibaba.fastjson.JSONObject;
import io.github.penn.rest.context.CurrentRequestContext;
import io.github.penn.rest.context.WebContext;
import io.github.penn.rest.exception.RestCallException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

/**
 * relay caller
 *
 * @author penn
 */
@Slf4j
@Component
public class RelayCaller {

    @Autowired
    private RestTemplate restTemplate;

    public RelayRequest parseRelayRequest(HttpServletRequest request, IRestRepeater restRepeater) {
        RelayRequest relayRequest = new RelayRequest();
        //get relay host
        String beforeUrl = request.getRequestURL().toString();

        HttpUrl.Builder builder = HttpUrl.get(beforeUrl).newBuilder();
        //host
        builder.host(restRepeater.repeatHost());
        //port
        builder.port(restRepeater.repeatPort());
        //params
        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
        //other setting
        relayRequest.setRelayUrl(builder.toString());
        relayRequest.setHttpMethod(httpMethod);

        //bodyJSON
        relayRequest.setBodyJson(new CurrentRequestContext(request).init().getBodyParams());

        return relayRequest;

    }


    /**
     * relay url
     */
    public RestResponse<JSONObject> relayCall(RelayRequest relayRequest) {
        RestResponse restResponse = new RestResponse<>();
        int requestId = relayRequest.hashCode();
        try {
            log.info("\n[RELAY-CALL-START-{}]\nMETHOD:{}, URL: {} \nPARAMS: {}",
                    requestId, relayRequest.getHttpMethod(), relayRequest.getRelayUrl(),
                    JSONObject.toJSONString(relayRequest.getBodyJson()));
            JSONObject jsonObject = doRelayCall(relayRequest);
            restResponse.setResponse(jsonObject);
        } catch (Exception e) {
            restResponse.setIfCallException(true);
            restResponse.setException(new RestCallException(e));
        } finally {
            log.info(
                    "\n[RELAY-CALL-END-{}]\nMETHOD:{}, URL: {}  \nIF-CALL-EXCEPTION: {} \nEXCEPTION:{} \nRESP: {}",
                    requestId, relayRequest.getHttpMethod(), relayRequest.getRelayUrl(),
                    restResponse.getIfCallException(), restResponse.getException(),
                    restResponse.getResponse());
        }
        return restResponse;
    }

    /**
     * do relay url
     */
    public JSONObject doRelayCall(RelayRequest relayRequest) {
        switch (relayRequest.getHttpMethod()) {
            case GET: {
                return restTemplate.getForObject(relayRequest.getRelayUrl(), JSONObject.class);
            }
            case POST: {
                return restTemplate.postForObject(relayRequest.getRelayUrl(),
                        relayRequest.getBodyJson(), JSONObject.class);
            }
            default: {
                log.warn("not support http method method: {}, context: {}", relayRequest.getHttpMethod(),
                        relayRequest);
            }
        }
        return WebJSON.fromWebContext().addParam("error", "not support method.");
    }


}
