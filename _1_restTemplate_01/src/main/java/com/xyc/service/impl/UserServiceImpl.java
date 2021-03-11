package com.xyc.service.impl;

import com.xyc.dao.UserDAO;
import com.xyc.entity.User;
import com.xyc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDAO uDAO;

    @Override
    public User getOne(Integer id) {
        return uDAO.getOne(id);
    }
}
