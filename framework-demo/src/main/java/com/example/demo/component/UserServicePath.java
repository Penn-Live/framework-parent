package com.example.demo.component;

import io.github.penn.rest.RestDomainPathSupport;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class UserServicePath extends RestDomainPathSupport {


    @Getter
    private String queryAllUser;


    public String getDomainKey() {
        return "user-info-service";
    }

    @PostConstruct
    protected void initUserUrls(){
        queryAllUser = addPath("/a/b/c");
    }



}
