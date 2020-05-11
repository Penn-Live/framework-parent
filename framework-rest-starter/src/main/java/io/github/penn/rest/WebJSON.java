package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import io.github.penn.rest.context.WebContext;

/**
 * @author tangzhongping
 */
public class WebJSON extends JSONObject {

    private JSONObject source;

    private WebJSON() {
    }

    private WebJSON(JSONObject jsonObject) {
        this.source = jsonObject;
    }


    public static WebJSON fromWebContext() {
        return from(WebContext.getBodyParamJSON());
    }

    public static WebJSON newJSON(){
        return from(new JSONObject());
    }

    public static WebJSON from(JSONObject jsonObject) {
        return new WebJSON(jsonObject);
    }


    public WebJSON peekBodyParam(String name) {
        this.put(name, source.get(name));
        return this;
    }

    public WebJSON transBodyParamName(String from, String to) {
        this.put(to, source.get(from));
        return this;
    }


    public WebJSON addParam(String name, Object value) {
        this.put(name, value);
        return this;
    }
}
