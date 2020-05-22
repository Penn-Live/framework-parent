package io.github.penn.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * rest configuration
 */
@Configuration
@ComponentScan("io.github.penn.rest")
@EnableConfigurationProperties(RestServiceDomainProperties.class)
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



    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean httpServletRequestTransFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(webContextSetter());
        registration.addUrlPatterns("/*");
        registration.setName("httpServletRequestTransFilter");
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



