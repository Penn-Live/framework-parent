package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.penn.rest.call.GetCall;
import io.github.penn.rest.call.PostCall;
import io.github.penn.rest.call.RestService;
import io.github.penn.rest.exception.RestCallException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * rest caller
 *
 * @author penn
 */
@Slf4j
@Component
public class RestCaller {

    @Autowired
    private ApplicationContext applicationContext;

    private Cache<RestService, RestTemplate> restTemplateCache =
            CacheBuilder.newBuilder().build();

    //todo:下个版本做校验，现在只约定
    public RequestContext parseRequestContext(Method method, Object[] args) {
        RequestContext requestContext = new RequestContext();

        RestService restServiceAnno =
                method.getDeclaringClass().getAnnotation(RestService.class);
        //parse rest template
        RestTemplate restTemplate = null;
        try {
            restTemplate = restTemplateCache.get(restServiceAnno,
                    () -> parseRestTemplate(restServiceAnno));
            requestContext.setRestTemplate(restTemplate);
        } catch (ExecutionException e) {
            throw new RestCallException(e);
        }

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
        completeUrl = resolvePlaceHolder(completeUrl);
        requestContext.setCompletedUrl(completeUrl);
        if (args.length > 0) {
            requestContext.setParams((JSONObject) args[0]);
        } else {
            requestContext.setParams(new JSONObject());
        }
        return requestContext;
    }


    /**
     * parse rest template
     *
     * @param restServiceAnno
     */
    private RestTemplate parseRestTemplate(RestService restServiceAnno) {
        String restBeanName = resolvePlaceHolder(restServiceAnno.restTemplate());
        //resolve the name if has placeholder
        RestTemplate restTemplate = applicationContext.getBean(restBeanName, RestTemplate.class);
        if (restTemplate != null) {
            return restTemplate;
        }
        // try defaults
        String[] restTemplateBeans = applicationContext.getBeanNamesForType(RestTemplate.class);
        if (restTemplateBeans != null && restTemplateBeans.length > 0) {
            return applicationContext.getBean(restTemplateBeans[0], RestTemplate.class);
        }
        throw new RestCallException("not restTemplate to using to call.");
    }


    public RestResponse<JSONObject> restCall(RequestContext context) {
        RestResponse<JSONObject> restResponse = new RestResponse<>();
        int reqeustId = context.hashCode();
        try {
            log.info("\n[REST-CALL-START-{}]\nMETHOD:{}, URL: {} \nPARAMS: {}",
                    reqeustId, context.getHttpMethod(), context.getCompletedUrl(),
                    JSONObject.toJSONString(context.getParams()));
            JSONObject jsonObject = doRestCall(context);
            restResponse.setResponse(jsonObject);
        } catch (Exception e) {
            restResponse.setIfCallException(true);
            restResponse.setException(new RestCallException(e));
        }
        log.info(
                "\n[REST-CALL-END-{}]\nMETHOD:{}, URL: {}  \nIF-CALL-EXCEPTION: {} \nEXCEPTION:{} \nRESP: {}",
                reqeustId, context.getHttpMethod(), context.getCompletedUrl(),
                restResponse.getIfCallException(), restResponse.getException(),
                restResponse.getResponse());
        return restResponse;
    }

    private String resolvePlaceHolder(String completeUrl) {
        return applicationContext.getEnvironment().resolvePlaceholders(completeUrl);
    }

    private JSONObject doRestCall(RequestContext context) {
        switch (context.getHttpMethod()) {
            case GET: {
                return context.getRestTemplate()
                        .getForObject(context.getCompletedUrl(), JSONObject.class, context.getParams());
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


}
