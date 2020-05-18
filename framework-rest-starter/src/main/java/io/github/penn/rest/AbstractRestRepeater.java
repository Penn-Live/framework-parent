package io.github.penn.rest;

import okhttp3.HttpUrl;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * abstract rest repeater
 */
public abstract class AbstractRestRepeater implements IRestRepeater {


    @Override
    public boolean support(String url) {
        return true;
    }

    @Override
    public void preRelaying() {

    }

    @Override
    public void afterRelaying(RestResponse restResponse) {

    }

    @Override
    public WebJSON callErrorDefault() {
        return WebJSON.newJSON().addParam("retCode", "99999")
                .addParam("retMsg", "server error.");
    }

    @Override
    public void ifNullRelayCall(HttpServletResponse response) throws IOException {
        response.getWriter().write(callErrorDefault().toString());
        response.flushBuffer();
    }

    protected HttpUrl getHttpUrl(String url) {
        return HttpUrl.parse(url);
    }
}
