package com.example.mysql.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mysql.model.User;
import com.example.mysql.model.UserPlus;
import com.example.mysql.service.JdbcTemplateExampleService;
import com.example.mysql.service.MyBatisExampleService;
import com.example.mysql.service.MyBatisPlusExampleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库框架示例控制器
 * 清晰展示JDBC Template、MyBatis、MyBatis Plus三种框架的使用方式
 */
@Slf4j
@RestController
@RequestMapping("/mysql/framework")
public class FrameworkExampleController {

    @Autowired
    private JdbcTemplateExampleService jdbcTemplateExampleService;

    @Autowired
    private MyBatisExampleService myBatisExampleService;

    @Autowired
    private MyBatisPlusExampleService myBatisPlusExampleService;

    // ==================== JDBC Template示例 ====================

    @PostMapping("/jdbc/user")
    public User jdbcCreateUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam Integer age) {
        return jdbcTemplateExampleService.createUser(username, email, age);
    }

    @GetMapping("/jdbc/user/{id}")
    public User jdbcGetUser(@PathVariable Long id) {
        return jdbcTemplateExampleService.getUserById(id);
    }

    @GetMapping("/jdbc/users")
    public List<User> jdbcGetAllUsers() {
        return jdbcTemplateExampleService.getAllUsers();
    }

    @PutMapping("/jdbc/user/{id}")
    public User jdbcUpdateUser(@PathVariable Long id,
                               @RequestParam String username,
                               @RequestParam String email,
                               @RequestParam Integer age) {
        return jdbcTemplateExampleService.updateUser(id, username, email, age);
    }

    @DeleteMapping("/jdbc/user/{id}")
    public String jdbcDeleteUser(@PathVariable Long id) {
        boolean result = jdbcTemplateExampleService.deleteUser(id);
        return result ? "JDBC Template删除成功" : "JDBC Template删除失败";
    }

    @GetMapping("/jdbc/users/age/{age}")
    public List<User> jdbcGetUsersByAge(@PathVariable Integer age) {
        return jdbcTemplateExampleService.getUsersByAge(age);
    }

    @GetMapping("/jdbc/count")
    public String jdbcGetUserCount() {
        int count = jdbcTemplateExampleService.getUserCount();
        return "JDBC Template用户总数: " + count;
    }

    @PostMapping("/jdbc/batch")
    public String jdbcBatchInsert() {
        User user1 = new User();
        user1.setUsername("jdbc_batch_1");
        user1.setEmail("jdbc1@example.com");
        user1.setAge(20);

        User user2 = new User();
        user2.setUsername("jdbc_batch_2");
        user2.setEmail("jdbc2@example.com");
        user2.setAge(25);

        jdbcTemplateExampleService.batchInsertUsers(Arrays.asList(user1, user2));
        return "JDBC Template批量插入成功";
    }

    @GetMapping("/jdbc/users/conditions")
    public List<User> jdbcGetUsersByConditions(@RequestParam(required = false) String username,
                                               @RequestParam(required = false) Integer minAge,
                                               @RequestParam(required = false) Integer maxAge) {
        return jdbcTemplateExampleService.getUsersByConditions(username, minAge, maxAge);
    }

    // ==================== MyBatis示例 ====================

    @PostMapping("/mybatis/user")
    public User myBatisCreateUser(@RequestParam String username,
                                  @RequestParam String email,
                                  @RequestParam Integer age) {
        return myBatisExampleService.createUser(username, email, age);
    }

    @GetMapping("/mybatis/user/{id}")
    public User myBatisGetUser(@PathVariable Long id) {
        return myBatisExampleService.getUserById(id);
    }

    @GetMapping("/mybatis/users")
    public List<User> myBatisGetAllUsers() {
        return myBatisExampleService.getAllUsers();
    }

    @PutMapping("/mybatis/user/{id}")
    public User myBatisUpdateUser(@PathVariable Long id,
                                  @RequestParam String username,
                                  @RequestParam String email,
                                  @RequestParam Integer age) {
        return myBatisExampleService.updateUser(id, username, email, age);
    }

    @DeleteMapping("/mybatis/user/{id}")
    public String myBatisDeleteUser(@PathVariable Long id) {
        boolean result = myBatisExampleService.deleteUser(id);
        return result ? "MyBatis删除成功" : "MyBatis删除失败";
    }

    @GetMapping("/mybatis/user/username/{username}")
    public User myBatisGetUserByUsername(@PathVariable String username) {
        return myBatisExampleService.getUserByUsername(username);
    }

    // ==================== MyBatis Plus示例 ====================

    @PostMapping("/mybatis-plus/user")
    public UserPlus myBatisPlusCreateUser(@RequestParam String username,
                                          @RequestParam String email,
                                          @RequestParam Integer age) {
        return myBatisPlusExampleService.createUser(username, email, age);
    }

    @GetMapping("/mybatis-plus/user/{id}")
    public UserPlus myBatisPlusGetUser(@PathVariable Long id) {
        return myBatisPlusExampleService.getUserById(id);
    }

    @GetMapping("/mybatis-plus/users")
    public List<UserPlus> myBatisPlusGetAllUsers() {
        return myBatisPlusExampleService.getAllUsers();
    }

    @PutMapping("/mybatis-plus/user/{id}")
    public UserPlus myBatisPlusUpdateUser(@PathVariable Long id,
                                          @RequestParam String username,
                                          @RequestParam String email,
                                          @RequestParam Integer age) {
        return myBatisPlusExampleService.updateUser(id, username, email, age);
    }

    @DeleteMapping("/mybatis-plus/user/{id}")
    public String myBatisPlusDeleteUser(@PathVariable Long id) {
        boolean result = myBatisPlusExampleService.deleteUser(id);
        return result ? "MyBatis Plus删除成功" : "MyBatis Plus删除失败";
    }

    @GetMapping("/mybatis-plus/users/age/{age}")
    public List<UserPlus> myBatisPlusGetUsersByAge(@PathVariable Integer age) {
        return myBatisPlusExampleService.getUsersByAge(age);
    }

    @GetMapping("/mybatis-plus/user/username/{username}")
    public UserPlus myBatisPlusGetUserByUsername(@PathVariable String username) {
        return myBatisPlusExampleService.getUserByUsername(username);
    }

    @GetMapping("/mybatis-plus/users/conditions")
    public List<UserPlus> myBatisPlusGetUsersByConditions(@RequestParam(required = false) String username,
                                                          @RequestParam(required = false) Integer minAge,
                                                          @RequestParam(required = false) Integer maxAge) {
        return myBatisPlusExampleService.getUsersByConditions(username, minAge, maxAge);
    }

    @GetMapping("/mybatis-plus/users/page")
    public IPage<UserPlus> myBatisPlusGetUsersByPage(@RequestParam(defaultValue = "1") int current,
                                                      @RequestParam(defaultValue = "10") int size) {
        return myBatisPlusExampleService.getUsersByPage(current, size);
    }

    @GetMapping("/mybatis-plus/users/page/condition")
    public IPage<UserPlus> myBatisPlusGetUsersByPageWithCondition(@RequestParam(defaultValue = "1") int current,
                                                                   @RequestParam(defaultValue = "10") int size,
                                                                   @RequestParam(required = false) Integer minAge) {
        return myBatisPlusExampleService.getUsersByPageWithCondition(current, size, minAge);
    }

    @PostMapping("/mybatis-plus/batch")
    public String myBatisPlusBatchInsert() {
        UserPlus user1 = new UserPlus();
        user1.setUsername("mp_batch_1");
        user1.setEmail("mp1@example.com");
        user1.setAge(20);

        UserPlus user2 = new UserPlus();
        user2.setUsername("mp_batch_2");
        user2.setEmail("mp2@example.com");
        user2.setAge(25);

        myBatisPlusExampleService.batchInsertUsers(Arrays.asList(user1, user2));
        return "MyBatis Plus批量插入成功";
    }

    @PutMapping("/mybatis-plus/batch/age")
    public String myBatisPlusBatchUpdateAge(@RequestParam Integer oldAge,
                                            @RequestParam Integer newAge) {
        boolean result = myBatisPlusExampleService.batchUpdateAgeByLambda(oldAge, newAge);
        return result ? "MyBatis Plus批量更新成功" : "MyBatis Plus批量更新失败";
    }

    @GetMapping("/mybatis-plus/count/age/{age}")
    public long myBatisPlusCountByAge(@PathVariable Integer age) {
        return myBatisPlusExampleService.countByAge(age);
    }

    @GetMapping("/mybatis-plus/exists/username/{username}")
    public boolean myBatisPlusExistsByUsername(@PathVariable String username) {
        return myBatisPlusExampleService.existsByUsername(username);
    }

    // ==================== 框架对比 ====================

    @GetMapping("/compare/create")
    public Map<String, String> compareCreate() {
        Map<String, String> result = new HashMap<>();
        result.put("JDBC Template", "需要手写完整SQL: INSERT INTO user(...) VALUES(?, ?, ...)");
        result.put("MyBatis", "需要手写SQL注解: @Insert(\"INSERT INTO user(...) VALUES(...)\")");
        result.put("MyBatis Plus", "无需SQL: save(entity) - 自动生成INSERT语句");
        result.put("推荐", "MyBatis Plus最简洁，适合快速开发");
        return result;
    }

    @GetMapping("/compare/query")
    public Map<String, String> compareQuery() {
        Map<String, String> result = new HashMap<>();
        result.put("JDBC Template", "手写SQL: SELECT * FROM user WHERE id = ?");
        result.put("MyBatis", "手写SQL注解: @Select(\"SELECT * FROM user WHERE id = #{id}\")");
        result.put("MyBatis Plus", "无需SQL: getById(id) - 自动生成SELECT语句");
        result.put("推荐", "MyBatis Plus最便捷，MyBatis适合复杂SQL");
        return result;
    }

    @GetMapping("/compare/condition")
    public Map<String, String> compareCondition() {
        Map<String, String> result = new HashMap<>();
        result.put("JDBC Template", "手动拼接SQL: StringBuilder + 参数列表");
        result.put("MyBatis", "使用XML或注解，需要手写SQL");
        result.put("MyBatis Plus", "条件构造器: LambdaQueryWrapper - 类型安全，无需手写SQL");
        result.put("推荐", "MyBatis Plus的条件构造器最强大");
        return result;
    }

    @GetMapping("/compare/features")
    public Map<String, String> compareFeatures() {
        Map<String, String> result = new HashMap<>();
        result.put("JDBC Template", "✓ 直接SQL控制  ✗ 无ORM  ✗ 需要手动处理结果映射");
        result.put("MyBatis", "✓ ORM映射  ✓ 复杂SQL支持  ✗ 需要手写SQL  ✗ 无分页插件");
        result.put("MyBatis Plus", "✓ 自动CRUD  ✓ 条件构造器  ✓ 分页插件  ✓ 字段填充  ✗ 复杂SQL需要MyBatis");
        result.put("总结", "MyBatis Plus = MyBatis + 增强功能，适合90%的CRUD场景");
        return result;
    }
}

