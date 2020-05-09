package com.example.demo.rest.service;

import com.alibaba.fastjson.JSONObject;
import io.github.penn.rest.call.GetCall;
import io.github.penn.rest.call.RestService;

@RestService(domain = "/demoService")
public interface DemoRestService {


    @GetCall(path = "/demo")
    JSONObject demoMethod();

}
