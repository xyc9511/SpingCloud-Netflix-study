package com.xyc.controller;



import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.xyc.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/consumer")
@Slf4j
@DefaultProperties(defaultFallback = "defauleFallBackMethod")
public class TestController {

    public String defauleFallBackMethod(){
        return "可对整个类的方法进行降级处理。。。。";
    }

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/{id}")
    //设置服务无法访问时，采用myFallBack方法快速返回失败结果
    @HystrixCommand(fallbackMethod = "myFallBack")
    public String getOne(@PathVariable("id") Integer id) {
        if(id==1){
            throw new RuntimeException("请求为1，太忙，无法访问");
        }

        String url = "http://user-service/rest/"+id;
        System.out.println("url = " + url);
        return restTemplate.getForObject(url, String.class);
    }

    public String myFallBack(Integer id){
        System.out.println("id查询失败。。。。。id: "+id);

        return "网络紧急升级。。";
    }

}
