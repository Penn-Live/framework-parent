package io.github.penn.rest.mapper;

/**
 * Object properties injector
 */
public class PropertiesInjector  {

    private  Object target;
    private PropertiesInjector(){}

    public  static <T> PropertiesInjector to(T t){
        PropertiesInjector propertiesInjector=new PropertiesInjector();
        propertiesInjector.target=t;
        return propertiesInjector;
    }

    public PropertiesInjector from(Object source){


        return this;
    }


    public PropertiesInjector from(Object source,InjectorMapping injectorMapping){
        return this;
    }


}
