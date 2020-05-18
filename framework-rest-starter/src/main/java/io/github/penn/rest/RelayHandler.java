package io.github.penn.rest;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author tangzhongping
 */
@AllArgsConstructor
@Slf4j
public class RelayHandler extends OncePerRequestFilter {


    RelayCaller relayCaller;
    List<IRestRepeater> restRepeaters;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (CollectionUtils.isEmpty(restRepeaters)) {
            filterChain.doFilter(request, response);
        }
        IRestRepeater restRepeater = getRepeater(request);
        if (restRepeater == null) {
            filterChain.doFilter(request, response);
            return;
        }
        //pre call
        restRepeater.preRelaying();
        //call relay
        RelayRequest relayRequest = relayCaller.parseRelayRequest(request, restRepeater);
        //relay call
        RestResponse<JSONObject> restResponse = relayCaller.relayCall(relayRequest);
        //throw if call fail
        if (restResponse.getIfCallException()) {
            writeResponse(restRepeater.callErrorDefault(), response);
            return;
        }
        //post call
        restRepeater.afterRelaying(restResponse);
        //write
        if (restResponse.getResponse() != null) {
            writeResponse(restResponse.getResponse(), response);
        } else {
            //if empty call
            restRepeater.ifNullRelayCall(response);
        }
    }


    private void writeResponse(JSONObject rp, HttpServletResponse response) throws IOException {
        if (rp != null) {
            response.getWriter().write(rp.toString());
            response.flushBuffer();
        }
    }


    private IRestRepeater getRepeater(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        for (IRestRepeater restRepeater : restRepeaters) {
            if (restRepeater.support(url)) {
                return restRepeater;
            }
        }
        return null;
    }


}
