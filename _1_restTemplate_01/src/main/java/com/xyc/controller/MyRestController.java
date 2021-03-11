package com.xyc.controller;

import com.xyc.entity.User;
import com.xyc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/rest")
public class MyRestController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public User getOne(@PathVariable("id") Integer id){
        User one = userService.getOne(id);
        return one;
    }

}
