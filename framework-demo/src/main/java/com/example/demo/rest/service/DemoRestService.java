package com.example.demo.rest.service;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.model.DemoResponse;
import io.github.penn.rest.RestResponse;
import io.github.penn.rest.WebJSON;

public interface DemoRestService {


    RestResponse<DemoResponse> demoMethod(JSONObject jsonObject);

    RestResponse<JSONObject> demoMethod2(WebJSON name);
}
