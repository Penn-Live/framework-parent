package io.github.penn.rest.mapper;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Object properties injector
 */
public class PropertiesInjector<T> {

    private T target;
    private static JSONObject EMPTY = new JSONObject();

    private PropertiesInjector() {
    }

    private static LoadingCache<Class<?>, Map<String, Field>>
            injectCaching = CacheBuilder.newBuilder()
            .build(new CacheLoader<Class<?>, Map<String, Field>>() {
                @Override
                public Map<String, Field> load(Class<?> aClass) throws Exception {
                    return loadingClassFieldCache(aClass);
                }
            });
    ;

    public static <T> PropertiesInjector to(T t) {
        PropertiesInjector propertiesInjector = new PropertiesInjector();
        propertiesInjector.target = t;

        return propertiesInjector;

    }






    private PropertiesInjector doInject(Object sourceProperty, Object targetProperty, InjectorMapping injectorMapping) {

        Object fieldValue = JSONPath.eval(sourceProperty, injectorMapping.getSourcePath());
        if (fieldValue == null) {
            return this;
        }
        try {
            Map<String, Field> fieldMap = injectCaching.get(targetProperty.getClass());
            Field field = fieldMap.get(injectorMapping.getTargetPath());

            //1.common type 2 common type
            if (isCommonType(field.getType()) && isCommonType(fieldValue.getClass())) {
                field.set(targetProperty, ConvertUtils.convert(fieldValue, field.getType()));
                return this;
            }
            //2. object 2 object
            if (isObjectType(field.getType()) && isObjectType(fieldValue.getClass())) {
                //empty but not null field value
                Object fieldPropertyObject = JSONObject.toJavaObject(EMPTY, field.getType());

                field.set(targetProperty, fieldPropertyObject);
                return this;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return this;
    }


    private static Map<String, Field> loadingClassFieldCache(Class<?> aClass) {
        Set<Field> allFields = ReflectionUtils.getAllFields(aClass);
        if (CollectionUtils.isEmpty(allFields)) {
            return Collections.EMPTY_MAP;
        }
        Map<String, Field> fieldMap = Maps.newHashMap();
        //cache fields
        for (Field field : allFields) {
            field.setAccessible(true);
            //current field
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
    }


    /**
     * 判断是否是可以直接赋值的类型,String 和 其他普通类型
     */
    private static boolean isCommonType(Class<?> aClass) {
        return (String.class == aClass)
                || (ClassUtils.isPrimitiveOrWrapper(aClass));
    }

    private static boolean isListType(Class<?> aClass) {
        return !isCommonType(aClass) && (List.class.isAssignableFrom(aClass));
    }

    private static boolean isObjectType(Class<?> aClass) {
        return (!isCommonType(aClass)) && (!isListType(aClass));
    }


}
