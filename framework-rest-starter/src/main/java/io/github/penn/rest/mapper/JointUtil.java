package io.github.penn.rest.mapper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.google.common.collect.Lists;
import io.github.penn.rest.exception.JointException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author tangzhongping
 */
public class JointUtil {

    private static final JSONObject EMPTY = new JSONObject();
    private static final String EMPTY_DOMAIN = "";

    public static <T> boolean isJointClass(T t) {
        if (t == null) {
            return false;
        }
        return t.getClass().isAnnotationPresent(Joint.class);
    }


    public static <T> Joint getClassJointAnn(T t) {
        if (t == null) {
            return null;
        }

        if (isJointClass(t)) {
            return t.getClass().getAnnotation(Joint.class);
        }
        return null;
    }


    /**
     * joint
     */
    public static <T> T joint(T target, Object source, String domain) {
        if (target == null || source == null) {
            throw new JointException("empty target");
        }

        Class<?> targetClass = target.getClass();
        Joint classJoint = getClassJointAnn(target);
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            Joint fieldJoint = declaredField.getAnnotation(Joint.class);
            JointEntity jointEntity = getJointEntity(classJoint, fieldJoint);
            if (jointEntity == null||jointEntity.skip) {
                continue;
            }

            if (!StringUtils.equals(jointEntity.getDomain(), domain)) {
                continue;
            }
            //process joint
            String exp = jointEntity.getUsing();
            if (StringUtils.isEmpty(exp)) {
                exp = declaredField.getName();
            }
            //exp a.b[0].c
            //get data from JsonObject
            if (!StringUtils.startsWith(exp, "$.")) {
                exp = "$." + StringUtils.removeStart(exp, ".");
            }
            String fieldName = declaredField.getName();
            String sourcePath = "$." + fieldName;

            if (JSONPath.contains(source, exp)) {
                Object fieldValue = JSONPath.eval(source, exp);
                //1.List类型
                if (List.class.isAssignableFrom(declaredField.getType())
                    &&List.class.isAssignableFrom(fieldValue.getClass())) {
                    List dataArray = (List) fieldValue;
                    List list = Lists.newArrayList();
                    Class classInObject = resolveGenericClass(declaredField);
                    for (Object value : dataArray) {
                        //new object
                        Object objInList = JSONObject.toJavaObject(EMPTY, classInObject);
                        //fill object
                        objInList=joint(objInList, value, EMPTY_DOMAIN);
                        list.add(objInList);
                    }
                    //set value
                    fieldValue=list;

                } else if ((String.class != fieldValue.getClass())
                        && (!ClassUtils.isPrimitiveOrWrapper(fieldValue.getClass()))) {
                    //empty but not null field value
                    Object fieldTarget = JSONObject.toJavaObject(EMPTY, declaredField.getType());
                    fieldValue = joint(fieldTarget, fieldValue, EMPTY_DOMAIN);
                    //fieldValue = JSONObject.toJavaObject((JSONObject) fieldValue, declaredField.getType());
                }

                //set value
                try {
                    JSONPath.set(target, sourcePath, fieldValue);
                } catch (Exception e) {
                    try {
                        BeanUtils.setProperty(target, fieldName, fieldValue);
                    } catch (Exception exception) {
                        throw new JointException("cant set value for property=" + fieldName);
                    }
                }
            }
        }
        return target;
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
     * get joint entity
     */
    public static JointEntity getJointEntity(Joint classJoint, Joint fieldJoint) {
        if (fieldJoint == null) {
            if (classJoint == null) {
                return null;
            } else {
                return new JointEntity(classJoint.domain(), classJoint.using(),classJoint.skip());
            }
        } else {
            if (classJoint == null) {
                return new JointEntity(fieldJoint.domain(), fieldJoint.using(),fieldJoint.skip());
            } else {
                return new JointEntity(StringUtils.defaultIfBlank(fieldJoint.domain(), classJoint.domain()),
                        StringUtils.defaultIfBlank(fieldJoint.using(), classJoint.using()),fieldJoint.skip());
            }
        }

    }


    @Getter
    @AllArgsConstructor
    static class JointEntity {
        String domain;
        String using;
        boolean skip;
    }
}
