package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import io.github.penn.rest.call.GetCall;
import io.github.penn.rest.call.PostCall;
import io.github.penn.rest.call.RestService;
import io.github.penn.rest.exception.RestCallException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * rest service proxy
 *
 * @author tangzhongping
 */
@Slf4j
public class RestServiceProxy<T> implements InvocationHandler {

    private Class<T> interfaceType;
    private RestTemplate restTemplate;
    private ApplicationContext applicationContext;

    public RestServiceProxy(Class<T> interfaceType, RestTemplate restTemplate,ApplicationContext applicationContext) {
        this.interfaceType = interfaceType;
        this.restTemplate = restTemplate;
        this.applicationContext=applicationContext;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        RequestContext requestContext = parseRequestContext(method, args);
        return restCall(requestContext);
    }


    //todo:下个版本做校验，现在只约定
    private RequestContext parseRequestContext(Method method, Object[] args) {
        RequestContext requestContext = new RequestContext();

        //JSONObject params= (JSONObject) args[0];
        RestService restServiceAnno =
                method.getDeclaringClass().getAnnotation(RestService.class);

        Annotation restCallAnno = method.getDeclaredAnnotations()[0];
        String path = "";
        if (GetCall.class.equals(restCallAnno.annotationType())) {
            GetCall getCall = (GetCall) restCallAnno;
            path = getCall.path();
            requestContext.setHttpMethod(HttpMethod.GET);
        }

        if (PostCall.class.equals(restCallAnno.annotationType())) {
            PostCall postCall = (PostCall) restCallAnno;
            path = postCall.path();
            requestContext.setHttpMethod(HttpMethod.POST);
        }

        String completeUrl = StringUtils.removeEnd(restServiceAnno.domain(), "/") + "/"
                + StringUtils.removeStart(path, "/");
        //resolvePlaceholders
        completeUrl = applicationContext.getEnvironment().resolvePlaceholders(completeUrl);
        requestContext.setCompletedUrl(completeUrl);
        if (args.length > 0) {
            requestContext.setParams((JSONObject) args[0]);
        } else {
            requestContext.setParams(new JSONObject());
        }
        return requestContext;
    }


    private RestResponse<JSONObject> restCall(RequestContext context) {
        RestResponse<JSONObject> restResponse = new RestResponse<>();
        try {
            log.info("[REST-CALL] request context: {}", context);
            JSONObject jsonObject = doRestCall(context);
            restResponse.setResponse(jsonObject);
        } catch (Exception e) {
            restResponse.setIfCallException(true);
            restResponse.setException(new RestCallException(e));
        }
        log.info("[REST-CALL] request url:{}, response: {}", context.getCompletedUrl(), restResponse);
        return restResponse;
    }

    private JSONObject doRestCall(RequestContext context) {
        switch (context.getHttpMethod()) {
            case GET: {
                return restTemplate.getForObject(context.getCompletedUrl(), JSONObject.class, context.getParams());
            }
            case POST: {
                return restTemplate.postForObject(context.getCompletedUrl(), context.getParams(), JSONObject.class);
            }
        }
        return WebJSON.newJSON().addParam("error", "not support method.");
    }
}