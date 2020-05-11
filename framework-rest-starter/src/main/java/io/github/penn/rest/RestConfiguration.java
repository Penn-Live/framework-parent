package io.github.penn.rest;

import java.nio.charset.Charset;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * rest configuration
 */
@Configuration
@ComponentScan("io.github.penn.rest")
@EnableConfigurationProperties
public class RestConfiguration extends WebMvcConfigurationSupport {


    @Bean
    @ConditionalOnMissingBean
    public WebContextSetter webContextSetter(){
        return new WebContextSetter();
    }

    @Bean("restTemplate")
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate;
    }



    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(webContextSetter()).addPathPatterns("/**");
        super.addInterceptors(interceptorRegistry);
    }
}
