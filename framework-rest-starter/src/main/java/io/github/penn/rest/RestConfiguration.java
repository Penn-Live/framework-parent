package io.github.penn.rest;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartResolver;
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
    public RequestCondition requestCondition() {
        return new RequestCondition();
    }


    @Bean
    @ConditionalOnMissingBean
    public WebContextSetter webContextSetter() {
        return new WebContextSetter(requestCondition());
    }

    @Bean("relayHandler")
    @ConditionalOnMissingBean
    public RelayHandler relayHandler(RelayCaller relayCaller, List<IRestRepeater> restRepeaters) {
        return new RelayHandler(relayCaller, restRepeaters);
    }


    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean httpServletRequestTransFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(webContextSetter());
        registration.addUrlPatterns("/*");
        registration.setName("httpServletRequestTransFilter");
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean relayRequestTransFilter(RelayHandler relayHandler) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(relayHandler);
        registration.addUrlPatterns("/*");
        registration.setName("relayRequestTransFilter");
        //after httpServletRequestTransFilter
        registration.setOrder(Integer.MIN_VALUE );
        return registration;
    }


    @Bean("restTemplate")
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate;
    }

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(webContextSetter()).addPathPatterns("/**");
        super.addInterceptors(interceptorRegistry);
    }


}



