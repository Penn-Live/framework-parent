package com.example.demo.rest.service;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.model.DemoResponse;
import io.github.penn.rest.RestResponse;
import io.github.penn.rest.WebJSON;
import io.github.penn.rest.call.GetCall;
import io.github.penn.rest.call.RestService;

@RestService(domain = "${baseUrl:http://p.3.cn}")
public interface DemoRestService {


    @GetCall(path = "/prices/mgets?skuIds=J",hint = "DEMO1 服务")
    RestResponse<DemoResponse> demoMethod(JSONObject jsonObject);

    @GetCall(path = "/demo2",hint = "DEMO2 服务")
    RestResponse<JSONObject> demoMethod2(WebJSON name);
}
