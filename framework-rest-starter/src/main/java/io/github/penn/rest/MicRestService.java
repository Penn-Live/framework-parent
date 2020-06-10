package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import io.github.penn.rest.mapper.InjectorMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.function.Function;

/**
 * @author tangzhongping
 */
@Data
public class MicRestService {

    private String url;
    private RestServiceCaller restServiceCaller;
    private String domain = Strings.EMPTY;
    private Function<RestResponse, Boolean> ifBisErrorFunction=(e)-> false;

    private String retCodeParam;
    private String retMsgParam;

    public MicRestService(String url, RestServiceCaller restServiceCaller) {
        this.url = url;
        this.restServiceCaller = restServiceCaller;
    }

    public MicRestService(String url, RestServiceCaller restServiceCaller,
                          Function<RestResponse, Boolean> ifBisErrorFunction) {
        this.url = url;
        this.restServiceCaller = restServiceCaller;
        this.ifBisErrorFunction = ifBisErrorFunction;
    }

    public MicRestService(String url, RestServiceCaller restServiceCaller, String domain) {
        this.url = url;
        this.restServiceCaller = restServiceCaller;
        this.domain = domain;
    }

    public MicRestService usingRetCode(String retCodeParam){
        this.retCodeParam=retCodeParam;
        return this;
    }

    public MicRestService usingRetMsg(String retMsgParam){
        this.retMsgParam=retMsgParam;
        return this;
    }


    public <P> RestResponse<JSONObject> postCall(P params) {
        return ifBisError(restServiceCaller.postCall(url, params));
    }


    public <P> RestResponse<JSONObject> getCall(P params) {
        return ifBisError(restServiceCaller.getGall(url, params));
    }

    public <T, P> RestResponse<JSONObject> getJoint(P params, T target) {
        return ifBisError(restServiceCaller.getJoint(url, params, target, domain));
    }

    public <T, P> RestResponse<JSONObject> postJoint(P params, T target) {
        return ifBisError(restServiceCaller.postJoint(url, params, target, domain));
    }


    public <T, P> RestResponse<JSONObject> getJoint(P params, T target, String domain) {
        return ifBisError(restServiceCaller.getJoint(url, params, target, domain));
    }

    public <T, P> RestResponse<JSONObject> postJoint(P params, T target, String domain) {
        return ifBisError(restServiceCaller.postJoint(url, params, target, domain));
    }

    public <T, P> RestResponse<JSONObject> getInject(P params, T target) {
        return ifBisError(restServiceCaller.getInject(url, params, target));

    }

    public <T, P> RestResponse<JSONObject> postInject(P params, T target) {
        return  ifBisError(restServiceCaller.postInject(url, params, target));
    }


    public <T, P> RestResponse<JSONObject> getInject(P params, T target, InjectorMapping.PathMapping pathMapping) {
        return  ifBisError(restServiceCaller.getInject(url, params, target, pathMapping));
    }





    public RestResponse ifBisError(RestResponse restResponse){
        if (ifBisErrorFunction!=null) {
            restResponse.setIfBisException(ifBisErrorFunction.apply(restResponse));
        }
        if (StringUtils.isNotEmpty(retCodeParam)) {
            restResponse.setBisRetCode(String.valueOf(JSONPath.eval(restResponse.response,retCodeParam)));
        }

        if (StringUtils.isNotEmpty(retMsgParam)) {
            restResponse.setBisRetMsg(String.valueOf(JSONPath.eval(restResponse.response,retMsgParam)));
        }
        return restResponse;
    }


}
