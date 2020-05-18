package io.github.penn.rest;

import lombok.extern.slf4j.Slf4j;

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
    private RestServiceCaller restServiceCaller;

    public RestServiceProxy(Class<T> interfaceType, RestServiceCaller restServiceCaller) {
        this.interfaceType = interfaceType;
         this.restServiceCaller = restServiceCaller;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        //parse the request context
        RequestContext requestContext = restServiceCaller.parseRequestContext(method, args);
        //call request context
        return restServiceCaller.restCall(requestContext);
    }
}