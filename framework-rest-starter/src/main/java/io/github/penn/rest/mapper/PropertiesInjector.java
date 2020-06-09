package io.github.penn.rest.mapper;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import io.github.penn.rest.mapper.InjectorMapping.PathMapping;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
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
        doInjectObject(source, target);
        return;
      }
      //
    }
  }


  public Object findAndSetProperties(Object source, Object target, String targetPath) {
    //parse more paths
    int firstPathIndex = targetPath.indexOf(".");
    String currentPath = targetPath;
    if (firstPathIndex != -1) {
      currentPath = currentPath.substring(0, firstPathIndex);
    }
    try {
      Map<String, Field> fieldMap = injectCaching.get(target.getClass());
      if (!fieldMap.containsKey(currentPath)) {
        //error path,return
        return null;
      }
      Field field = fieldMap.get(currentPath);
      Object targetPropertyValue = field.get(target);
      if (isObjectType(field.getType()) && targetPropertyValue == null) {
        //instance
        targetPropertyValue = JSONObject.toJavaObject(EMPTY, field.getType());





      }


    } catch (Exception e) {
      //todo:
    }
    return null;
  }

  /**
   * inject object
   */
  private PropertiesInjector doInjectObject(Object sourceObj, Object targetObj) {
    //2. object 2 object
    if (isObjectType(targetObj.getClass()) && isObjectType(sourceObj.getClass())) {
      try {
        Map<String, Field> fieldMap = injectCaching.get(targetObj.getClass());
        Set<Entry<String, Field>> fieldsEntries = fieldMap.entrySet();
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
            doInjectObject(fieldValue, fieldPropertyObject);
            //set properties
            field.set(targetObj, fieldPropertyObject);
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
        field.set(targetObj, ConvertUtils.convert(targetObj, field.getType()));
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
