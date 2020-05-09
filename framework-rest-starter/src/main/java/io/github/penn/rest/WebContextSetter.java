package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import io.github.penn.rest.context.CurrentRequestContext;
import io.github.penn.rest.context.WebContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author tangzhongping
 */
@Slf4j
public class WebContextSetter implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        log.info("[WebContextSetter] parse request info for request method:{}", ((HandlerMethod) handler).getMethod().getName());
        //request
        CurrentRequestContext currentRequestContext = new CurrentRequestContext();
        currentRequestContext.setRequest(httpServletRequest);
        currentRequestContext.setSession(httpServletRequest.getSession());

        //set web params
        JSONObject bodyParams = parseBodyParams(httpServletRequest);
        currentRequestContext.setBodyParams(bodyParams);

        //add request
        WebContext.addCurrentRequestContext(currentRequestContext);
        return true;
    }


    /**
     * body params
     */
    private JSONObject parseBodyParams(HttpServletRequest httpServletRequest) {
        try {
            String bodyStr = IOUtils.toString(httpServletRequest.getReader());
            //simple way
            if (StringUtils.contains(bodyStr, "{")) {
                return JSONObject.parseObject(bodyStr);
            }
        } catch (IOException e) {
            //error is all time thing
        }
        return new JSONObject();
    }


    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }


    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //release the object
        WebContext.removeCurrentRequest();
    }
}
