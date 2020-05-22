package com.example.demo.component;

import io.github.penn.rest.RestDomainPathSupport;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class UserServicePath extends RestDomainPathSupport {
    public String getDomainKey() {
        return "user-info-service";
    }


    @Getter
    private String queryAllUser;

    @PostConstruct
    protected void initUserUrls(){
        queryAllUser = addPath("/a/b/c");
    }



}
