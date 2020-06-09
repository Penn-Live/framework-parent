package io.github.penn.rest.mapper;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.ToString;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class JSONPathTest {


    public static void main(String[] args) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        JSONObject person = getPerson();

        Person person1=new Person();
        PropertiesInjector
                .to(person1)
                .injectRoot(person);
        System.out.println(person1);
    }

    public static JSONObject getPerson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "360731");
        jsonObject.put("name", "penn");

        JSONObject kid1 = new JSONObject();
        kid1.put("id", null);
        kid1.put("name", "pp");

        JSONObject kid2 = new JSONObject();
        kid2.put("id", "123456");
        kid2.put("name", "kk");
        kid2.put("name2", "kk");
        kid2.put("name4", "kk");

        ArrayList<Object> objects = Lists.newArrayList();
        objects.add(kid1);
        objects.add(kid2);
        jsonObject.put("kids", objects);

        jsonObject.put("kid", kid1);
        return jsonObject;
    }


}

@Data
@ToString
class Person {
    private Integer id;
    private String name;


    private Person kid;
    private List<Person> kids;

}