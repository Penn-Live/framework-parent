package io.github.penn.rest.context;

import com.alibaba.fastjson.JSONObject;
import io.github.penn.rest.exception.InvalidParamException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 表示请求上下文
 *
 * @author tangzhongping
 */
@Slf4j
public class WebContext {


  /**
   * 当前请求信息
   */
  private static final ThreadLocal<CurrentRequestContext> currentRequest = new ThreadLocal<CurrentRequestContext>();

  private WebContext() {
  }


  /**
   * set a current request info
   */
  public static void addCurrentRequestContext(CurrentRequestContext currentRequestContext) {
    currentRequest.set(currentRequestContext);
    log.info("[WebContext] set current request info: {}", currentRequestContext.getBodyParams());
  }


  /**
   * get current request
   */
  public static HttpServletRequest getRequest() {
    CurrentRequestContext currentRequestContext = currentRequest.get();
    if (currentRequestContext != null) {
      return currentRequestContext.getRequest();
    }
    return null;
  }


  /**
   * get current session
   */
  public static HttpSession getSession() {
    CurrentRequestContext currentRequestContext = currentRequest.get();
    if (currentRequestContext != null) {
      return currentRequestContext.getSession();
    }
    return null;
  }


  /**
   * get body ParamJSON
   */
  public static JSONObject getBodyParamJSON() {
    CurrentRequestContext currentRequestContext = currentRequest.get();
    if (currentRequestContext != null) {
      return currentRequestContext.getBodyParams();
    }
    return new JSONObject();
  }


  /**
   * get body params in string
   */
  public static String getBodyParamString(String paramsName) {
    CurrentRequestContext currentRequestContext = currentRequest.get();
    if (currentRequestContext != null) {
      return currentRequestContext.getBodyParams().getString(paramsName);
    }
    return null;
  }

  /**
   * remove the currentRequest
   */
  public static void removeCurrentRequest() {
    currentRequest.remove();
  }


  /**
   * bodyParams must has this params or throws exception
   */
  public static void bodyParamMustHas(String... names) throws InvalidParamException {
    CurrentRequestContext currentRequestContext = currentRequest.get();
    if (currentRequestContext != null) {
      JSONObject bodyParams = currentRequestContext.getBodyParams();
      for (String name : names) {
        if (!bodyParams.containsKey(name)) {
          throw new InvalidParamException("no body param=" + name + " in the body params.");
        }
      }
      return;
    }
    throw new InvalidParamException("all name are empty");
  }

  /**
   * bodyParams must has this params or throws exception
   */
  public static void bodyParamMustNotEmpty(String... names) {
    CurrentRequestContext currentRequestContext = currentRequest.get();
    if (currentRequestContext != null) {
      JSONObject bodyParams = currentRequestContext.getBodyParams();
      for (String name : names) {
        if ((!bodyParams.containsKey(name)) || (StringUtils.isEmpty(bodyParams.getString(name)))) {
          throw new InvalidParamException("empty body param=" + name + " in the body params.");
        }
      }
      return;
    }
    throw new InvalidParamException("all name are empty");
  }


  /**
   * set sessionAttr
   */
  public static void setSessionAttr(String name, Object value) {
    CurrentRequestContext currentRequestContext = currentRequest.get();
    if (currentRequestContext != null) {
      currentRequestContext.getSession().setAttribute(name, value);
    }
    log.warn("no current request context find, please check the config.");
  }


  /**
   * get sessionAttr
   */
  public static Object getSessionAttr(String name) {
    CurrentRequestContext currentRequestContext = currentRequest.get();
    if (currentRequestContext != null) {
      return currentRequestContext.getSession().getAttribute(name);
    }
    log.warn("no current request context find, please check the config.");
    return null;
  }


  /**
   * get sessionAttr string
   */
  public static String getSessionAttrString(String name) {
    return (String) getSessionAttr(name);
  }

}
