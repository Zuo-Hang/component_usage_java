package com.example.mysql.service;

import com.example.mysql.mapper.UserMapper;
import com.example.mysql.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MyBatis使用示例服务类
 * 演示原生MyBatis（使用注解方式）的使用
 */
@Slf4j
@Service("myBatisExampleService")
public class MyBatisExampleService {

    @Autowired
    private UserMapper userMapper;

    /**
     * MyBatis插入用户 - 需要手动编写SQL
     */
    @Transactional
    public User createUser(String username, String email, Integer age) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setAge(age);
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        int result = userMapper.insert(user);
        log.info("[MyBatis] 插入用户结果: {}, 用户ID: {}", result, user.getId());
        return user;
    }

    /**
     * MyBatis根据ID查询用户 - 需要手动编写SQL
     */
    public User getUserById(Long id) {
        User user = userMapper.findById(id);
        log.info("[MyBatis] 查询用户ID: {}, 结果: {}", id, user);
        return user;
    }

    /**
     * MyBatis更新用户 - 需要手动编写SQL
     */
    @Transactional
    public User updateUser(Long id, String username, String email, Integer age) {
        User user = userMapper.findById(id);
        if (user != null) {
            user.setUsername(username);
            user.setEmail(email);
            user.setAge(age);
            user.setUpdateTime(LocalDateTime.now());
            int result = userMapper.update(user);
            log.info("[MyBatis] 更新用户结果: {}", result);
            return userMapper.findById(id);
        }
        return null;
    }

    /**
     * MyBatis查询所有用户 - 需要手动编写SQL
     */
    public List<User> getAllUsers() {
        List<User> users = userMapper.findAll();
        log.info("[MyBatis] 查询所有用户，数量: {}", users.size());
        return users;
    }

    /**
     * MyBatis根据用户名查询 - 需要手动编写SQL
     */
    public User getUserByUsername(String username) {
        User user = userMapper.findByUsername(username);
        log.info("[MyBatis] 根据用户名查询: {}, 结果: {}", username, user);
        return user;
    }

    /**
     * MyBatis根据ID删除用户 - 需要手动编写SQL
     */
    @Transactional
    public boolean deleteUser(Long id) {
        int result = userMapper.deleteById(id);
        log.info("[MyBatis] 删除用户ID: {}, 结果: {}", id, result);
        return result > 0;
    }
}

