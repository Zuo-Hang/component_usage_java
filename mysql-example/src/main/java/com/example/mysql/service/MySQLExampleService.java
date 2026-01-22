package com.example.mysql.service;

import com.example.mysql.mapper.UserMapper;
import com.example.mysql.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MySQL使用示例服务类
 * 演示JDBC和MyBatis的使用
 */
@Slf4j
@Service
public class MySQLExampleService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * MyBatis操作示例 - 插入用户
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
        log.info("插入用户结果: {}, 用户ID: {}", result, user.getId());
        return user;
    }

    /**
     * MyBatis操作示例 - 查询用户
     */
    public User getUserById(Long id) {
        User user = userMapper.findById(id);
        log.info("查询用户ID: {}, 结果: {}", id, user);
        return user;
    }

    /**
     * MyBatis操作示例 - 更新用户
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
            log.info("更新用户结果: {}", result);
            return userMapper.findById(id);
        }
        return null;
    }

    /**
     * MyBatis操作示例 - 查询所有用户
     */
    public List<User> getAllUsers() {
        List<User> users = userMapper.findAll();
        log.info("查询所有用户，数量: {}", users.size());
        return users;
    }

    /**
     * JDBC Template操作示例 - 直接SQL查询
     */
    public int getUserCount() {
        String sql = "SELECT COUNT(*) FROM user";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        log.info("用户总数: {}", count);
        return count != null ? count : 0;
    }

    /**
     * JDBC Template操作示例 - 批量操作
     */
    @Transactional
    public void batchInsertUsers(List<User> users) {
        String sql = "INSERT INTO user(username, email, age, create_time, update_time) " +
                     "VALUES(?, ?, ?, ?, ?)";
        
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.batchUpdate(sql, users, users.size(), (ps, user) -> {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setInt(3, user.getAge());
            ps.setObject(4, now);
            ps.setObject(5, now);
        });
        
        log.info("批量插入用户数量: {}", users.size());
    }

    /**
     * 事务示例 - 确保数据一致性
     */
    @Transactional
    public void transferUserData(Long fromId, Long toId) {
        User fromUser = userMapper.findById(fromId);
        User toUser = userMapper.findById(toId);
        
        if (fromUser != null && toUser != null) {
            // 模拟数据转移操作
            log.info("从用户 {} 转移数据到用户 {}", fromId, toId);
            // 如果任何一步失败，整个事务会回滚
        }
    }
}

