package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.rest.service.DemoRestService;
import io.github.penn.rest.context.WebContext;
import io.github.penn.rest.WebJSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/penn")
public class DemoController {

    @Autowired
    private DemoRestService demoRestService;

    @PostMapping("/demo")
    public JSONObject demo() {
        WebContext.bodyParamMustHas("name", "abc");
        return WebJSON.newJSON()
                .peekBodyParam("name")
                .peekBodyParam("age");
    }


    @PostMapping("/demo2")
    public JSONObject demo2() {

        return demoRestService.demoMethod();
    }

}
