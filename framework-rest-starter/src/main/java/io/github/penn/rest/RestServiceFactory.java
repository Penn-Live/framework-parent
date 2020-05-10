package io.github.penn.rest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author tangzhongping
 * Rest service facotry
 */
public class RestServiceFactory<T> implements FactoryBean<T> {

    private Class<T> interfaceType;

    /**
     * inject by application
     */
    @Getter
    @Setter
    private RestCaller restCaller;


    public RestServiceFactory(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    @Override
    public T getObject() throws Exception {
        //jkd proxy
        InvocationHandler handler = new RestServiceProxy<>(interfaceType,restCaller);
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