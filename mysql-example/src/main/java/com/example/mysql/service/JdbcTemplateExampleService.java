package com.example.mysql.service;

import com.example.mysql.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JDBC Template使用示例服务类
 * 演示Spring JDBC Template的直接SQL操作
 */
@Slf4j
@Service("jdbcTemplateExampleService")
public class JdbcTemplateExampleService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * JDBC Template插入用户 - 直接编写SQL
     */
    @Transactional
    public User createUser(String username, String email, Integer age) {
        String sql = "INSERT INTO user(username, email, age, create_time, update_time) " +
                     "VALUES(?, ?, ?, ?, ?)";
        
        LocalDateTime now = LocalDateTime.now();
        int result = jdbcTemplate.update(sql, username, email, age, now, now);
        log.info("[JDBC Template] 插入用户结果: {}", result);

        // 查询刚插入的用户
        String selectSql = "SELECT * FROM user WHERE username = ? ORDER BY id DESC LIMIT 1";
        User user = jdbcTemplate.queryForObject(selectSql, 
                new BeanPropertyRowMapper<>(User.class), username);
        log.info("[JDBC Template] 插入的用户ID: {}", user.getId());
        return user;
    }

    /**
     * JDBC Template根据ID查询 - 直接编写SQL
     */
    public User getUserById(Long id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        User user = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(User.class), id);
        log.info("[JDBC Template] 查询用户ID: {}, 结果: {}", id, user);
        return user;
    }

    /**
     * JDBC Template更新用户 - 直接编写SQL
     */
    @Transactional
    public User updateUser(Long id, String username, String email, Integer age) {
        String sql = "UPDATE user SET username = ?, email = ?, age = ?, update_time = ? WHERE id = ?";
        LocalDateTime now = LocalDateTime.now();
        int result = jdbcTemplate.update(sql, username, email, age, now, id);
        log.info("[JDBC Template] 更新用户结果: {}", result);
        
        return getUserById(id);
    }

    /**
     * JDBC Template查询所有用户 - 直接编写SQL
     */
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM user ORDER BY id DESC";
        List<User> users = jdbcTemplate.query(sql, 
                new BeanPropertyRowMapper<>(User.class));
        log.info("[JDBC Template] 查询所有用户，数量: {}", users.size());
        return users;
    }

    /**
     * JDBC Template条件查询
     */
    public List<User> getUsersByAge(Integer age) {
        String sql = "SELECT * FROM user WHERE age = ?";
        List<User> users = jdbcTemplate.query(sql, 
                new BeanPropertyRowMapper<>(User.class), age);
        log.info("[JDBC Template] 根据年龄查询: {}, 结果数量: {}", age, users.size());
        return users;
    }

    /**
     * JDBC Template统计查询
     */
    public int getUserCount() {
        String sql = "SELECT COUNT(*) FROM user";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        log.info("[JDBC Template] 用户总数: {}", count);
        return count != null ? count : 0;
    }

    /**
     * JDBC Template批量插入
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
        
        log.info("[JDBC Template] 批量插入用户数量: {}", users.size());
    }

    /**
     * JDBC Template根据ID删除
     */
    @Transactional
    public boolean deleteUser(Long id) {
        String sql = "DELETE FROM user WHERE id = ?";
        int result = jdbcTemplate.update(sql, id);
        log.info("[JDBC Template] 删除用户ID: {}, 结果: {}", id, result);
        return result > 0;
    }

    /**
     * JDBC Template复杂查询 - 多条件
     */
    public List<User> getUsersByConditions(String username, Integer minAge, Integer maxAge) {
        StringBuilder sql = new StringBuilder("SELECT * FROM user WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();
        
        if (username != null && !username.isEmpty()) {
            sql.append(" AND username LIKE ?");
            params.add("%" + username + "%");
        }
        if (minAge != null) {
            sql.append(" AND age >= ?");
            params.add(minAge);
        }
        if (maxAge != null) {
            sql.append(" AND age <= ?");
            params.add(maxAge);
        }
        sql.append(" ORDER BY create_time DESC");
        
        List<User> users = jdbcTemplate.query(sql.toString(), 
                new BeanPropertyRowMapper<>(User.class), params.toArray());
        log.info("[JDBC Template] 复杂条件查询，结果数量: {}", users.size());
        return users;
    }
}

