package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * rest service proxy
 *
 * @author tangzhongping
 */
public class RestServiceProxy<T> implements InvocationHandler {

    private Class<T> interfaceType;
    private RestTemplate restTemplate;

    public RestServiceProxy(Class<T> interfaceType, RestTemplate restTemplate) {
        this.interfaceType = interfaceType;
        this.restTemplate = restTemplate;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }



        System.out.println("调用前，参数：{}" + args);
        Object result = JSONObject.toJSONString(args);
        System.out.println("调用后，结果：{}" + result);
        return result;
    }
}