package io.github.penn.rest;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author tangzhongping
 * Rest service facotry
 */
public class RestServiceFactory<T> implements FactoryBean<T> {

    private Class<T> interfaceType;

    @Getter
    @Setter
    private RestTemplate restTemplate;

    public RestServiceFactory(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    @Override
    public T getObject() throws Exception {
        //jkd proxy
        InvocationHandler handler = new RestServiceProxy<>(interfaceType,restTemplate);
        return (T) Proxy.newProxyInstance(interfaceType.getClassLoader(),
                new Class[]{interfaceType}, handler);
    }

    @Override
    public Class<T> getObjectType() {
        return interfaceType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}