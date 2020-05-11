package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import io.github.penn.rest.call.GetCall;
import io.github.penn.rest.call.PostCall;
import io.github.penn.rest.call.RestService;
import io.github.penn.rest.exception.RestCallException;
import lombok.Setter;
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
    private RestCaller restCaller;

    public RestServiceProxy(Class<T> interfaceType, RestCaller restCaller) {
        this.interfaceType = interfaceType;
         this.restCaller=restCaller;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        //parse the request context
        RequestContext requestContext = restCaller.parseRequestContext(method, args);
        //call request context
        return restCaller.restCall(requestContext);
    }
}