# framework-parent
a framework parent easy to support java application


#usage
```$xslt
@SpringBootApplication
@EnableRestService(basePackage = "com.example.demo.rest.service")
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
```
在Controller或者对应的方法中加入 @InjectWebContext
```$xslt
@RestController
@RequestMapping("/penn")
@InjectWebContext
public class DemoController {

    @Autowired
    private DemoRestService demoRestService;

    @PostMapping("/demo")
    public JSONObject demo() {
        //WebContext.bodyParamMustHas("name", "abc");
        HttpServletRequest request = WebContext.getRequest();

        return WebJSON.fromWebContext()
                .peekBodyParam("name")
                .peekBodyParam("age");
    }
}
```


关于RestService

```$xslt
@RestService(domain = "${baseUrl:http://baidu.com}")
public interface DemoRestService {


    @GetCall(path = "/demo")
    RestResponse<JSONObject> demoMethod(JSONObject jsonObject);

}

```
