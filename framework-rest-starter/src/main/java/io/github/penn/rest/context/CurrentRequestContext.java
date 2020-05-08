package io.github.penn.rest.context;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author tangzhongping
 */
@Getter
@Setter
public class CurrentRequestContext {

    private HttpServletRequest request;
    private HttpSession session;


    /**
     * BODY请求参数
     */
    private JSONObject bodyParams=new JSONObject();

}
