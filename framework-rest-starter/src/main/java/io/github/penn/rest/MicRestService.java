package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;

/**
 * @author tangzhongping
 */
@Data
public class MicRestService {

    private String url;
    private RestServiceCaller restServiceCaller;
    private String domain= Strings.EMPTY;

    public MicRestService(String url, RestServiceCaller restServiceCaller) {
        this.url = url;
        this.restServiceCaller = restServiceCaller;
    }

    public MicRestService(String url, RestServiceCaller restServiceCaller, String domain) {
        this.url = url;
        this.restServiceCaller = restServiceCaller;
        this.domain = domain;
    }

    public<P> RestResponse<JSONObject> postCall(P params){
        return restServiceCaller.postCall(url, params);
    }


    public<P> RestResponse<JSONObject> getCall(P params){
        return restServiceCaller.getGall(url, params);
    }

    public<T,P> RestResponse<JSONObject> getJoint(P params,T target){
        return restServiceCaller.getJoint(url, params,target,domain);
    }

    public<T,P> RestResponse<JSONObject> postJoint(P params,T target){
        return restServiceCaller.postJoint(url, params,target,domain);
    }


    public<T,P> RestResponse<JSONObject> getJoint(P params,T target,String domain){
        return restServiceCaller.getJoint(url, params,target,domain);
    }

    public<T,P> RestResponse<JSONObject> postJoint(P params,T target,String domain){
        return restServiceCaller.postJoint(url, params,target,domain);
    }

}
