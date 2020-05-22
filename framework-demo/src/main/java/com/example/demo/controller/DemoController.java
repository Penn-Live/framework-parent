package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.entity.JointClass;
import com.example.demo.model.DemoResponse;
import com.example.demo.rest.service.DemoRestService;
import io.github.penn.rest.*;
import io.github.penn.rest.context.WebContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/penn")
@InjectWebContext
public class DemoController implements InitializingBean {

    private DemoRestService demoRestService;
    @Autowired
    private RestServiceCaller restServiceCaller;

    private MicRestService douBanService;

    @PostMapping("/demo")
    public DemoResponse demo() {
        //WebContext.bodyParamMustHas("name", "abc");
        HttpServletRequest request = WebContext.getRequest();

        WebJSON webJSON = WebJSON.fromWebContext()
                .peekBodyParam("name")
                .peekBodyParam("age");
        RestResponse<DemoResponse> restResponse = demoRestService.demoMethod(webJSON);
        return restResponse.getResponse();
    }


    @PostMapping("/demo2")
    public RestResponse demo2(@RequestBody JSONObject jsonObject) {
        RestResponse<DemoResponse> restResponse = demoRestService.demoMethod(WebJSON.fromWebContext().peekBodyParam("name"));
        return restResponse;
    }

    @PostMapping("/demo3")
    public JointClass demo3(@RequestBody JSONObject jsonObject){
        JointClass jointClass = new JointClass();
        RestResponse<JSONObject> call =
                douBanService.getJoint(WebJSON.newJSON().addParam("q","spring"),jointClass);
        return jointClass;
    }

    @PostMapping("/demo4")
    public RestResponse demo4(@RequestBody JSONObject jsonObject) {
        RestResponse<JSONObject> restResponse = demoRestService.demoMethod2(WebJSON.fromWebContext().peekBodyParam("name"));
        return restResponse;
    }


    public void afterPropertiesSet() throws Exception {
        douBanService= new MicRestService("https://api.binstd.com/caipiao/query", restServiceCaller,"penn");
    }
}
