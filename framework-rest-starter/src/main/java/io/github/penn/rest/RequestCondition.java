package io.github.penn.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tangzhongping
 */
@Component
public class RequestCondition {


    @Autowired(required = false)
    private MultipartResolver multipartResolver;


    /**
     * if wrap
     * @param httpServletRequest
     * @return
     */
    public boolean ifWrapRepeatRequest(HttpServletRequest httpServletRequest) {
        if (multipartResolver == null || (!multipartResolver.isMultipart(httpServletRequest))) {
            return true;
        }
        return false;
    }


}
