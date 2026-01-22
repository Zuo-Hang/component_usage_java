package com.example.mysql.mapper;

import com.example.mysql.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    /**
     * 查询所有用户
     */
    @Select("SELECT * FROM user")
    List<User> findAll();

    /**
     * 插入用户
     */
    @Insert("INSERT INTO user(username, email, age, create_time, update_time) " +
            "VALUES(#{username}, #{email}, #{age}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 更新用户
     */
    @Update("UPDATE user SET username=#{username}, email=#{email}, age=#{age}, " +
            "update_time=#{updateTime} WHERE id=#{id}")
    int update(User user);

    /**
     * 根据ID删除用户
     */
    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);
}

