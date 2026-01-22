package com.example.mysql.controller;

import com.example.mysql.model.User;
import com.example.mysql.service.MySQLExampleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * MySQL示例控制器
 */
@Slf4j
@RestController
@RequestMapping("/mysql")
public class MySQLExampleController {

    @Autowired
    private MySQLExampleService mysqlExampleService;

    @PostMapping("/user")
    public User createUser(@RequestParam String username,
                          @RequestParam String email,
                          @RequestParam Integer age) {
        return mysqlExampleService.createUser(username, email, age);
    }

    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id) {
        return mysqlExampleService.getUserById(id);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return mysqlExampleService.getAllUsers();
    }

    @PutMapping("/user/{id}")
    public User updateUser(@PathVariable Long id,
                          @RequestParam String username,
                          @RequestParam String email,
                          @RequestParam Integer age) {
        return mysqlExampleService.updateUser(id, username, email, age);
    }

    @GetMapping("/count")
    public String getUserCount() {
        int count = mysqlExampleService.getUserCount();
        return "用户总数: " + count;
    }

    @PostMapping("/batch")
    public String batchInsertUsers() {
        User user1 = new User();
        user1.setUsername("batch_user_1");
        user1.setEmail("batch1@example.com");
        user1.setAge(20);

        User user2 = new User();
        user2.setUsername("batch_user_2");
        user2.setEmail("batch2@example.com");
        user2.setAge(25);

        mysqlExampleService.batchInsertUsers(Arrays.asList(user1, user2));
        return "批量插入用户成功";
    }
}

