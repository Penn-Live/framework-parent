package io.github.penn.rest.mapper;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.penn.rest.mapper.InjectorMapping.PathMapping;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;

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


    public void inject(Object rawSource, PathMapping pathMapping) {
        Map<String, String> mappings = pathMapping.readingMappings();

        Set<Entry<String, String>> pathEntries = mappings.entrySet();
        for (Entry<String, String> pathEntry : pathEntries) {
            String sourcePath = pathEntry.getKey();
            String targetPath = pathEntry.getValue();
            //eval source value
            Object source = rawSource;
            if (!InjectorMapping.isRootPath(sourcePath)) {
                source = JSONPath.eval(rawSource, sourcePath);
            }

            if (source == null) {
                continue;
            }

            //eval target path
            //1. if root
            if (InjectorMapping.isRootPath(targetPath)) {
                //root must a object
                doFillObject(source, target);
                return;
            } else {
                //not root
                findAndSetProperties(source, target, targetPath);
            }

        }
    }


    public Object findAndSetProperties(Object source, Object target, String targetPath) {
        //parse more paths
        int firstPathIndex = targetPath.indexOf(".");
        String currentPath = targetPath;
        String restPath = null;
        if (firstPathIndex != -1) {
            currentPath = targetPath.substring(0, firstPathIndex);
            restPath = targetPath.substring(firstPathIndex + 1);
        }
        try {
            Map<String, Field> fieldMap = injectCaching.get(target.getClass());
            if (!fieldMap.containsKey(currentPath)) {
                //error path,return
                return null;
            }
            Field field = fieldMap.get(currentPath);
            Object targetPropertyValue = field.get(target);
            if (isObjectType(field.getType())) {
                if (targetPropertyValue == null) {
                    //instance
                    targetPropertyValue = JSONObject.toJavaObject(EMPTY, field.getType());
                }
                if (StringUtils.isNotEmpty(restPath)) {
                    findAndSetProperties(source, targetPropertyValue, restPath);
                } else {
                    doFillObject(source, targetPropertyValue);
                }
                field.set(target, targetPropertyValue);
            } else if (isListType(field.getType())) {
                //inject list
                targetPropertyValue= doFillList(source,field);
                field.set(target, targetPropertyValue);
            } else if (isCommonType(field.getType())) {
                //common type, can't split anymore
                doInjectCommon(source, target, field);
            }
        } catch (Exception e) {
            //todo:
        }
        return null;
    }

    /**
     * inject list
     */
    private List doFillList(Object source, Field field) {
        List list = Lists.newArrayList();
        if (!isListType(source.getClass())) {
            return list;
        }
        Class classInList = resolveGenericClass(field);
        List sourceByList = (List) source;
        Iterator iterator = sourceByList.iterator();
        while (iterator.hasNext()) {
            Object listObject = iterator.next();
            Object targetObject = JSONObject.toJavaObject(EMPTY, classInList);
            doFillObject(listObject, targetObject);
            list.add(targetObject);
        }
        return list;
    }

    private static Class resolveGenericClass(Field declaredField) {
        Type genericType = declaredField.getGenericType();
        if (null == genericType) {
            return JSONObject.class;
        }
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            // 得到泛型里的class类型对象
            Class<?> actualTypeArgument = (Class<?>)pt.getActualTypeArguments()[0];
            return actualTypeArgument;
        }
        return JSONObject.class;
    }

    /**
     * inject object
     */
    private PropertiesInjector doFillObject(Object sourceObj, Object targetObj) {
        //2. object 2 object
        if (isObjectType(targetObj.getClass()) && isObjectType(sourceObj.getClass())) {
            try {
                Map<String, Field> targetFieldMap = injectCaching.get(targetObj.getClass());
                Set<Entry<String, Field>> fieldsEntries = targetFieldMap.entrySet();
                for (Entry<String, Field> fieldsEntry : fieldsEntries) {
                    String fieldName = fieldsEntry.getKey();
                    Field field = fieldsEntry.getValue();
                    //get fieldValue
                    Object fieldValue = PropertyUtils.getProperty(sourceObj, fieldName);
                    if (fieldValue == null) {
                        continue;
                    }
                    //1.is common type
                    if (isCommonType(field.getType()) && isCommonType(fieldValue.getClass())) {
                        field.set(targetObj, ConvertUtils.convert(fieldValue, field.getType()));
                        continue;
                    }

                    //2. is object
                    if (isObjectType(field.getType()) && isObjectType(fieldValue.getClass())) {
                        Object fieldPropertyObject = JSONObject.toJavaObject(EMPTY, field.getType());
                        //inject again
                        doFillObject(fieldValue, fieldPropertyObject);
                        //set properties
                        field.set(targetObj, fieldPropertyObject);
                        continue;
                    }

                    //3.is list
                    if (isListType(field.getType()) && isListType(fieldValue.getClass())) {
                        List list = doFillList(fieldValue, field);
                        field.set(targetObj,list);
                        continue;
                    }
                }

            } catch (Exception e) {
                //todo:log
            }
        }
        return this;
    }


    /**
     * inject common
     */
    private PropertiesInjector doInjectCommon(Object sourceObj, Object targetObj, Field field) {
        try {
            //1.common type 2 common type
            if (isCommonType(field.getType()) && isCommonType(sourceObj.getClass())) {
                field.set(targetObj, ConvertUtils.convert(sourceObj, field.getType()));
                return this;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }


    /**
     * loading class field cache
     *
     * @param aClass
     * @return
     */
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
