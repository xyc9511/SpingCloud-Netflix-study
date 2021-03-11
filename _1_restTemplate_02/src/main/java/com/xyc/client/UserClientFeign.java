package com.xyc.client;

import com.xyc.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//声明当前为feign客户端，
// 针对服务名为uer-service的地址进行处理
@FeignClient("user-service")
public interface UserClientFeign {

    //会拼出  http://user-service/rest/{id} 的链接
    @GetMapping("/rest/{id}")
    String selectOne(@PathVariable("id") Integer id);

}
