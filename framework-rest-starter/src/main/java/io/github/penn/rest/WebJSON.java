package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;

/**
 * @author tangzhongping
 */
public class WebJSON extends JSONObject {


    private WebJSON() {
    }


    public static WebJSON newJSON() {
        return new WebJSON();
    }


    public WebJSON peekBodyParam(String name) {
        this.put(name, WebContext.getBodyParamJSON().get(name));
        return this;
    }


    public WebJSON addParam(String name, Object value) {
        this.put(name, value);
        return this;
    }
}
