package io.github.penn.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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





    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(webContextSetter()).addPathPatterns("/**");
        super.addInterceptors(interceptorRegistry);
    }
}
