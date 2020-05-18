package io.github.penn.rest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * repeater
 */
public interface IRestRepeater {

    /**
     * support
     */
    boolean support(String url);

    /**
     * repeat domain
     */
    String repeatHost();

    /**
     * repeat port
     */
    default int repeatPort() {
        return 80;
    }

    /**
     * preRelaying
     */
    void preRelaying();

    /**
     * afterRelaying
     */
    void afterRelaying(RestResponse restResponse);

    /**
     * error default
     */
    default WebJSON callErrorDefault() {
        return WebJSON.newJSON();
    }

    /**
     * empty if relay call
     */
    void ifNullRelayCall(HttpServletResponse response) throws IOException;

}
