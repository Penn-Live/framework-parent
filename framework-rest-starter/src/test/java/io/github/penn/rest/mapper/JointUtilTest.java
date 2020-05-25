package io.github.penn.rest.mapper;

import com.alibaba.fastjson.JSONObject;

import java.util.Arrays;
import java.util.List;

public class JointUtilTest {

    public static void main(String[] args) {
        JointClass jointClass = new JointClass();

        JSONObject jsonObject=getJsonObj();
        jointClass.joint(jsonObject, "");

        System.out.println(jointClass);

        System.out.println(JointUtil.isJointClass(jointClass));

    }

    private static JSONObject getJsonObj() {
        JSONObject jsonObject=new JSONObject();
        JSONObject data = new JSONObject();

        data.put("a", "4");
        data.put("b", 2);
        data.put("c", 3);
        JSONObject inner=new JSONObject();
        inner.put("ac", "inner-aValue");
        data.put("d", inner);

        List<String> names = Arrays.asList("a", "b", "c");
        data.put("e", names);

        jsonObject.put("data", data);
        return jsonObject;
    }


}