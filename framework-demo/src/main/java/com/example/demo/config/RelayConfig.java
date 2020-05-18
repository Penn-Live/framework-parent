package com.example.demo.config;

import io.github.penn.rest.AbstractRestRepeater;
import org.springframework.stereotype.Component;

@Component
public class RelayConfig extends AbstractRestRepeater {

    String host = "api.apiopen.top";

    public String repeatHost() {
        return host;
    }

    @Override
    public boolean support(String url) {
        return url.indexOf("/getJoke") > 0;
    }
}
