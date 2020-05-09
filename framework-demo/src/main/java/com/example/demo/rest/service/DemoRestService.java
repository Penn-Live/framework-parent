package com.example.demo.rest.service;

import com.alibaba.fastjson.JSONObject;
import io.github.penn.rest.RestResponse;
import io.github.penn.rest.call.GetCall;
import io.github.penn.rest.call.RestService;

@RestService(domain = "${baseUrl:http://baidu.com}")
public interface DemoRestService {


    @GetCall(path = "/demo")
    RestResponse<JSONObject> demoMethod(JSONObject jsonObject);

}
