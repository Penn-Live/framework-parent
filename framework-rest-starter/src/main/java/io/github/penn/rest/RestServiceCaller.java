package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import io.github.penn.rest.exception.RestCallException;
import io.github.penn.rest.mapper.InjectorMapping;
import io.github.penn.rest.mapper.JointUtil;
import io.github.penn.rest.mapper.PropertiesInjector;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * rest caller
 *
 * @author penn
 */
@Slf4j
@Component
@Scope("prototype")
public class RestServiceCaller {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RestTemplate defaultRestTemplate;

    private RestTemplate bindingRestTemplate;

    private ThreadLocal<RestTemplate> contextLocal = new ThreadLocal<>();


    public RestServiceCaller bindingRestTemplate(RestTemplate restTemplate){
        this.bindingRestTemplate=restTemplate;
        return this;
    }


    public RestServiceCaller usingRestTemplate(RestTemplate restTemplate) {
        contextLocal.set(restTemplate);
        return this;
    }

    /**
     * postCall
     */
    public <P> RestResponse<JSONObject> postCall(String url, P params) {
        RequestContext requestContext = parseRequestContext(HttpMethod.POST, url, parseJSONParams(params));
        return (RestResponse<JSONObject>) restCall(requestContext);
    }


    /**
     * get call
     */
    public <P> RestResponse<JSONObject> getGall(String url, P params) {
        RequestContext requestContext = parseRequestContext(HttpMethod.GET, url, parseJSONParams(params));
        return (RestResponse<JSONObject>) restCall(requestContext);
    }

    /**
     * post and joint
     */
    public <T, P> RestResponse<JSONObject> postJoint(String url, P params, T t, String domain) {
        RestResponse<JSONObject> restResponse = postCall(url, params);
        if (!restResponse.getIfCallException()) {
            JSONObject response = restResponse.getResponse();
            JointUtil.joint(t, response, domain);
        }
        return restResponse;
    }

    /**
     * get and joint
     */
    public <T, P> RestResponse<JSONObject> getJoint(String url, P params, T t, String domain) {
        RestResponse<JSONObject> restResponse = getGall(url, params);
        if (!restResponse.getIfCallException()) {
            JSONObject response = restResponse.getResponse();
            JointUtil.joint(t, response, domain);
        }
        return restResponse;
    }


    /**
     * get and inject
     */
    public <T, P> RestResponse<JSONObject> getInject(String url, P params, T t, InjectorMapping.PathMapping pathMapping) {
        RestResponse<JSONObject> restResponse = getGall(url, params);
        if (!restResponse.getIfCallException()) {
            JSONObject response = restResponse.getResponse();
            PropertiesInjector.to(t)
                    .inject(response, pathMapping);
        }
        return restResponse;
    }


    /**
     * get and inject
     */
    public <T, P> RestResponse<JSONObject> getInject(String url, P params, T t) {
        RestResponse<JSONObject> restResponse = getGall(url, params);
        if (!restResponse.getIfCallException()) {
            JSONObject response = restResponse.getResponse();
            PropertiesInjector.to(t)
                    .inject(response, InjectorMapping.DEFAULT_MAPPING);
        }
        return restResponse;
    }


    /**
     * post and joint
     */
    public <T, P> RestResponse<JSONObject> postInject(String url, P params, T t, InjectorMapping.PathMapping pathMapping) {
        RestResponse<JSONObject> restResponse = postCall(url, params);
        if (!restResponse.getIfCallException()) {
            JSONObject response = restResponse.getResponse();
            PropertiesInjector.to(t)
                    .inject(response, pathMapping);
        }
        return restResponse;
    }

    /**
     * post and joint
     */
    public <T, P> RestResponse<JSONObject> postInject(String url, P params, T t) {
        RestResponse<JSONObject> restResponse = postCall(url, params);
        if (!restResponse.getIfCallException()) {
            JSONObject response = restResponse.getResponse();
            PropertiesInjector.to(t)
                    .inject(response, InjectorMapping.DEFAULT_MAPPING);
        }
        return restResponse;
    }





    private RequestContext parseRequestContext(HttpMethod method, String completeUrl, JSONObject params) {
        RequestContext requestContext = new RequestContext();
        //parse return class
        requestContext.setReturnType(JSONObject.class);
        requestContext.setHttpMethod(method);

        //parse restTemplate
        RestTemplate restTemplate
                =ObjectUtils.firstNonNull(contextLocal.get(), this.bindingRestTemplate,this.defaultRestTemplate);
        requestContext.setRestTemplate(restTemplate);

        //resolvePlaceholders
        completeUrl = resolvePlaceHolder(completeUrl);
        requestContext.setCompletedUrl(completeUrl);
        requestContext.setParams(params == null ? new JSONObject() : params);
        return requestContext;
    }


    private <P> JSONObject parseJSONParams(P params) {
        if (params == null) {
            return null;
        }
        JSONObject p;
        if (!(params instanceof JSONObject)) {
            p = JSONObject.parseObject(JSONObject.toJSONString(params));
        } else {
            p = (JSONObject) params;
        }
        return p;
    }

    private RestResponse<?> restCall(RequestContext context) {
        RestResponse restResponse = new RestResponse<>();
        int requestId = context.hashCode();
        try {
            log.info("\n[REST-CALL-START-{}]\nHINT: {}\nMETHOD:{}, URL: {} \nPARAMS: {}",
                    requestId, context.getHint(), context.getHttpMethod(), context.getCompletedUrl(),
                    JSONObject.toJSONString(context.getParams()));
            JSONObject jsonObject = doRestCall(context);
            restResponse.setResponse(transReturnType(context, jsonObject));
        } catch (Exception e) {
            restResponse.setIfCallException(true);
            restResponse.setException(new RestCallException(e));
        } finally {
            //remove context restTemplate
            contextLocal.remove();
            log.info(
                    "\n[REST-CALL-END-{}]\nHINT: {}\nMETHOD:{}, URL: {}  \nIF-CALL-EXCEPTION: {} \nEXCEPTION:{} \nRESP: {}",
                    requestId, context.getHint(), context.getHttpMethod(), context.getCompletedUrl(),
                    restResponse.getIfCallException(), restResponse.getException(),
                    restResponse.getResponse());
        }
        return restResponse;
    }

    /**
     * trans return type
     */
    private Object transReturnType(RequestContext context, JSONObject source) {
        return source.toJavaObject(context.getReturnType());
    }

    private String resolvePlaceHolder(String completeUrl) {
        return applicationContext.getEnvironment().resolvePlaceholders(completeUrl);
    }

    private JSONObject doRestCall(RequestContext context) {
        switch (context.getHttpMethod()) {
            case GET: {
                return context.getRestTemplate()
                        .getForObject(constructUrl(context), JSONObject.class);
            }
            case POST: {
                return context.getRestTemplate()
                        .postForObject(context.getCompletedUrl(), context.getParams(), JSONObject.class);
            }
            default: {
                log.warn("not support http method method: {}, context: {}", context.getHttpMethod(),
                        context);
            }
        }
        return WebJSON.fromWebContext().addParam("error", "not support method.");
    }

    /**
     * construct url
     */
    private String constructUrl(RequestContext context) {
        HttpUrl.Builder builder = HttpUrl.get(context.getCompletedUrl()).newBuilder();
        Map<String, Object> paramsMap =
                context.getParams().getInnerMap();
        paramsMap.forEach((name, value) ->
                builder.addQueryParameter(name, String.valueOf(value)));
        return builder.toString();

    }


}
