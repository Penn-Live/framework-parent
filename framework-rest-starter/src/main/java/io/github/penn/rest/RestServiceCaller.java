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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
public class RestServiceCaller {

    @Autowired
    private ApplicationContext applicationContext;

    private Cache<RestService, RestTemplate> restTemplateCache =
            CacheBuilder.newBuilder().build();

    //todo:下个版本做校验，现在只约定
    public RequestContext parseRequestContext(Method method, Object[] args) throws ClassNotFoundException {
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

        //parse return class
        Class<?> returnClass = parseReturnClass(method);
        requestContext.setReturnType(returnClass);

        //http method
        if (GetCall.class.equals(restCallAnno.annotationType())) {
            GetCall getCall = (GetCall) restCallAnno;
            path = getCall.path();
            requestContext.setHttpMethod(HttpMethod.GET);
            requestContext.setHint(getCall.hint());
        }

        if (PostCall.class.equals(restCallAnno.annotationType())) {
            PostCall postCall = (PostCall) restCallAnno;
            path = postCall.path();
            requestContext.setHttpMethod(HttpMethod.POST);
            requestContext.setHint(postCall.hint());
        }

        String completeUrl = StringUtils.removeEnd(restServiceAnno.domain().trim(), "/") + "/"
                + StringUtils.removeStart(path.trim(), "/");
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
     * parse return class
     */
    private Class parseReturnClass(Method method) throws ClassNotFoundException {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
            return ClassUtils.getClass(actualTypeArguments[0].getTypeName());
        }
        //not a generic
        return ClassUtils.getClass(returnType.getTypeName());
    }


    /**
     * parse rest template
     *
     * @param restServiceAnno
     */
    private RestTemplate parseRestTemplate(RestService restServiceAnno) {
        String restBeanName = resolvePlaceHolder(restServiceAnno.restTemplate());
        RestTemplate restTemplate;
        if (StringUtils.isNotEmpty(restBeanName)) {
            //resolve the name if has placeholder
            restTemplate = applicationContext.getBean(restBeanName, RestTemplate.class);
            if (restTemplate != null) {
                return restTemplate;
            }
        }
        //try defaults
        restTemplate = applicationContext.getBean(RestTemplate.class);
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


    public RestResponse<?> restCall(RequestContext context) {
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
