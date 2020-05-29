package com.example.demo;

import io.github.penn.rest.RestServiceCaller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ABConfig {


    @Bean
    public RestServiceCaller restServiceCaller1(RestServiceCaller restServiceCaller){
        return restServiceCaller;
    }

    @Bean("restServiceCaller2")
    public RestServiceCaller restServiceCaller2(RestServiceCaller restServiceCaller){
        return restServiceCaller;
    }
}
