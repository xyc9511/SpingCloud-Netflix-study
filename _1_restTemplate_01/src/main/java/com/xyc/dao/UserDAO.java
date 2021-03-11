package com.xyc.dao;


import com.xyc.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserDAO {

    public User getOne(@Param("id") Integer id);

}
