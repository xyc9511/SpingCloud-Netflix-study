package com.xyc;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

//@EnableDiscoveryClient //开启eureka客户端发现功能
//@SpringBootApplication
//@EnableCircuitBreaker //开启熔断

@SpringCloudApplication //可代替上面3个注解
@EnableFeignClients //开启feign功能
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
