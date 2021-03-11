package com.xyc.controller;


import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.xyc.client.UserClientFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cf")
@Slf4j
public class ConsumerFeignController {

    @Autowired
    private UserClientFeign userClientFeign;

    @GetMapping("/{id}")
    @HystrixCommand(fallbackMethod = "myFallBack")
    public String getOne(@PathVariable Integer id){
        if(id==1){
            throw new RuntimeException("请求为1，太忙，无法访问");
        }

        return userClientFeign.selectOne(id);
    }

    public String myFallBack(Integer id){
        System.out.println("id查询失败。。。。。id:{}" + id);
//        log.error("id查询失败。。。。。id:{}",id);
        return "网络紧急升级。。";
    }
}
