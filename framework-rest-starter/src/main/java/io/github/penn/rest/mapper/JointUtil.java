package io.github.penn.rest.mapper;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import io.github.penn.rest.exception.JointException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

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
            if (jointEntity == null) {
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
            String sourcePath = "$." + declaredField.getName();

            if (JSONPath.contains(source, exp)) {
                Object fieldValue = JSONPath.eval(source, exp);
                if (fieldValue instanceof JSONObject) {
                    //empty but not null field value
                    Object fieldTarget = JSONObject.toJavaObject(EMPTY, declaredField.getType());
                    fieldValue = joint(fieldTarget, fieldValue, EMPTY_DOMAIN);
                    //fieldValue = JSONObject.toJavaObject((JSONObject) fieldValue, declaredField.getType());
                }
                JSONPath.set(target, sourcePath, fieldValue);
            }
        }
        return target;
    }


    /**
     * get joint entity
     */
    public static JointEntity getJointEntity(Joint classJoint, Joint fieldJoint) {
        if (fieldJoint == null) {
            if (classJoint == null) {
                return null;
            } else {
                return new JointEntity(classJoint.domain(), classJoint.using());
            }
        } else {
            if (classJoint == null) {
                return new JointEntity(fieldJoint.domain(), fieldJoint.using());
            } else {
                return new JointEntity(StringUtils.defaultIfBlank(fieldJoint.domain(), classJoint.domain()),
                        StringUtils.defaultIfBlank(fieldJoint.using(), classJoint.using()));
            }
        }

    }


    @Getter
    @AllArgsConstructor
    static class JointEntity {
        String domain;
        String using;
    }
}
