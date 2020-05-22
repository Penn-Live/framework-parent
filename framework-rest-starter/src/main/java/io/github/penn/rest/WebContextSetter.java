package io.github.penn.rest;

import io.github.penn.rest.context.CurrentRequestContext;
import io.github.penn.rest.context.WebContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

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
        //init if need
        initRequestConditionIfNeed();
        if (!requestCondition.ifInjectWebContextAnnotated(httpServletRequest)) {
            return true;
        }

        log.info("[WebContextSetter] parse request info for request method:{}",
                ((HandlerMethod) handler).getMethod().getName());
        //add request
        WebContext.addCurrentRequestContext(new CurrentRequestContext(httpServletRequest).init());
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
        if (requestCondition.ifInjectWebContextAnnotated(httpServletRequest)) {
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
        //init if need
        initRequestConditionIfNeed();
        if (requestCondition.ifWrapRepeatRequest(request)) {
            //trans request
            chainRequest = new RepeatedReadAbleRequest(chainRequest);
        }
        filterChain.doFilter(chainRequest, response);

    }

    private  void initRequestConditionIfNeed() {
        synchronized (RequestCondition.class){
            if (!requestCondition.isIfInit()) {
                WebApplicationContext webApplicationContext =
                        WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
                requestCondition.init(
                        webApplicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class));
            }

        }
    }

}
