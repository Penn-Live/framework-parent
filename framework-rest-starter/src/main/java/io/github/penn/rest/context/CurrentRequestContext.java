package io.github.penn.rest.context;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

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
    private JSONObject bodyParams = new JSONObject();

    public CurrentRequestContext() {
    }

    public CurrentRequestContext(HttpServletRequest request) {
        this.request = request;
        this.session = request.getSession();
    }

    /**
     * init
     */
    public CurrentRequestContext init(){
        this.bodyParams = parseBodyParams(request);
        return this;
    }

    /**
     * body params
     */
    public JSONObject parseBodyParams(HttpServletRequest httpServletRequest) {
        try {
            String bodyStr = IOUtils.toString(httpServletRequest.getReader());
            // judge body str in case not json format.
            if (StringUtils.isNotEmpty(bodyStr)
                    && JSONValidator.from(bodyStr).validate()) {
                return JSONObject.parseObject(bodyStr);
            }
        } catch (Exception e) {
            //error is all time thing
        }
        return new JSONObject();
    }

}
