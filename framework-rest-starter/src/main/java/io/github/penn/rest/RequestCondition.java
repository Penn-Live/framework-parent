package io.github.penn.rest;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author tangzhongping
 */
@Component
@Slf4j
public class RequestCondition {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * cache
     */
    private Cache<String, Boolean> webContextConditionCache =
            CacheBuilder.newBuilder().build();

    /**
     * all path to inject WebContext
     */
    private LinkedHashSet<String> injectWebContextSet = Sets.newLinkedHashSet();


    @PostConstruct
    public void initRequestCondition() {

        try {
            Map<RequestMappingInfo, HandlerMethod> handlerMethods
                    = requestMappingHandlerMapping.getHandlerMethods();
            Set<Map.Entry<RequestMappingInfo, HandlerMethod>> entries
                    = handlerMethods.entrySet();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : entries) {
                RequestMappingInfo requestMappingInfo = entry.getKey();
                HandlerMethod handlerMethod = entry.getValue();
                if (ifInjectWebContextAnnotatedFromHandlerMethod(handlerMethod)) {
                    Set<String> patterns =
                            requestMappingInfo.getPatternsCondition().getPatterns();
                    if (!CollectionUtils.isEmpty(patterns)) {
                        injectWebContextSet.addAll(patterns);
                    }
                }
                log.info("find inject web method: {}", injectWebContextSet);
            }

        } catch (Exception e) {
            log.error("init request condition error ", e);
        }

    }


    /**
     * if wrap
     *
     * @param httpServletRequest
     * @return
     */
    public boolean ifWrapRepeatRequest(HttpServletRequest httpServletRequest) {
        //for temp
        return ifInjectWebContextAnnotated(httpServletRequest);
    }

    /**
     * if injectWebContextAnnotated
     *
     * @param httpServletRequest
     * @return
     */
    public boolean ifInjectWebContextAnnotated(HttpServletRequest httpServletRequest) {
        String servletPath = httpServletRequest.getServletPath();
        return injectWebContextSet.contains(servletPath);
    }


    /**
     * if the web context need webContext
     */
    private boolean ifInjectWebContextAnnotatedFromHandlerMethod(HandlerMethod handlerMethod) {
        String cacheKey = handlerMethod.getBeanType().getName() + "." + handlerMethod.getMethod().getName();

        try {
            return webContextConditionCache.get(cacheKey, () -> {
                //try method
                InjectWebContext injectWebContext = handlerMethod.getMethod()
                        .getAnnotation(InjectWebContext.class);
                if (injectWebContext != null) {
                    return true;
                }
                //try class
                injectWebContext =
                        handlerMethod.getBeanType().getAnnotation(InjectWebContext.class);
                if (injectWebContext != null) {
                    return true;
                }
                return false;
            });
        } catch (ExecutionException e) {
            log.error("[RequestCondition] cache webContext setting error", e);
            //return false;
        }
        return false;
    }

}
