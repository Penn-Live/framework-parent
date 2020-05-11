package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import io.github.penn.rest.context.CurrentRequestContext;
import io.github.penn.rest.context.WebContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author tangzhongping
 */
@Slf4j
@AllArgsConstructor
public class WebContextSetter extends OncePerRequestFilter implements HandlerInterceptor {


    private RequestCondition requestCondition;


    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        if (!ifSetWebContext((HandlerMethod) handler)) {
            return true;
        }

        log.info("[WebContextSetter] parse request info for request method:{}",
                ((HandlerMethod) handler).getMethod().getName());
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


    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView)
            throws Exception {
    }


    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object handler, Exception e) throws Exception {
        if (ifSetWebContext((HandlerMethod) handler)) {
            //release the object
            log.info("[WebContextSetter] remove WebContext info for request method:{}",
                    ((HandlerMethod) handler).getMethod().getName());
            WebContext.removeCurrentRequest();
        }
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest chainRequest = request;

        if (requestCondition.ifWrapRepeatRequest(request)) {
            //trans request
            chainRequest = new RepeatedReadAbleRequest(chainRequest);
        }
        filterChain.doFilter(chainRequest, response);

    }


    /**
     * if the web context need webContext
     */
    private boolean ifSetWebContext(HandlerMethod handlerMethod) {
        InjectWebContext injectWebContext = handlerMethod.getMethod()
                .getAnnotation(InjectWebContext.class);
        if (injectWebContext != null) {
            return true;
        }
        injectWebContext =
                handlerMethod.getBeanType().getAnnotation(InjectWebContext.class);
        if (injectWebContext != null) {
            return true;
        }
        return false;
    }


    /**
     * body params
     */
    private JSONObject parseBodyParams(HttpServletRequest httpServletRequest) {
        try {
            String bodyStr = IOUtils.toString(httpServletRequest.getReader());
            // judge body str incase not json format.
            if (StringUtils.isNotEmpty(bodyStr)
                    && JSONValidator.from(bodyStr).validate()) {
                return JSONObject.parseObject(bodyStr);
            }
        } catch (IOException e) {
            //error is all time thing
        }
        return new JSONObject();
    }


}
