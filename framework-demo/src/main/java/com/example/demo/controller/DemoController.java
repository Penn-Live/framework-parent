package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import io.github.penn.rest.WebContext;
import io.github.penn.rest.WebJSON;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/penn")
public class DemoController {


    @PostMapping("/demo")
    public JSONObject demo() {
        WebContext.bodyParamMustHas("name");
        return WebJSON.newJSON()
                .peekBodyParam("name")
                .peekBodyParam("age");
    }

}
