package com.xyc;

import com.xyc.dao.UserDAO;
import com.xyc.entity.User;
import com.xyc.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = RestTemplateApplication.class)
public class MyTest {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserService userService;

    @Test
    public void test01(){
        User one = userService.getOne(1);
        System.out.println("one = " + one);
    }
}
