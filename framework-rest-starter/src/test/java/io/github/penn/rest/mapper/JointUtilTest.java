package io.github.penn.rest.mapper;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

public class JointUtilTest {

    public static void main(String[] args) {
        JSONObject jsonObject = JSONObject.parseObject("{\"retCode\":\"2\",\"retMsg\":\"abc.\",\"data\":[{\"a\":\"1\",\"c\":\"success.\"},{\"a\":\"4\",\"c\":\"server success.\"},{\"a\":\"3\",\"c\":\"server success.\"}]}");

        NewJoint newJoint = new NewJoint();
        JointUtil.joint(newJoint, jsonObject, "");
        System.out.println(newJoint);

    }


    private static JSONObject getJsonObj() {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();

        data.put("a", "4");
        data.put("b", 2);
        data.put("c", 3);
        JSONObject inner = new JSONObject();
        inner.put("ac", "inner-aValue");
        data.put("d", inner);

        List<String> names = Arrays.asList("a", "b", "c");
        data.put("e", names);

        jsonObject.put("data", data);
        return jsonObject;
    }


}

@Joint
@Data
class NewJoint {
    private String retCode;
    private String retMsg;
    private List<InnerClass> data;

}

@Joint
@Data
class InnerClass {
    private String a;
    private String c;
}