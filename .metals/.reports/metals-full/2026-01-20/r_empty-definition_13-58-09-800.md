error id: file://<WORKSPACE>/mysql-example/src/main/java/com/example/mysql/service/MyBatisPlusExampleService.java:com/baomidou/mybatisplus/core/metadata/IPage#
file://<WORKSPACE>/mysql-example/src/main/java/com/example/mysql/service/MyBatisPlusExampleService.java
empty definition using pc, found symbol in pc: com/baomidou/mybatisplus/core/metadata/IPage#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 372
uri: file://<WORKSPACE>/mysql-example/src/main/java/com/example/mysql/service/MyBatisPlusExampleService.java
text:
```scala
package com.example.mysql.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IP@@age;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mysql.mapper.UserPlusMapper;
import com.example.mysql.model.UserPlus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MyBatis Plus使用示例服务类
 * 演示MyBatis Plus的强大功能：自动CRUD、条件构造器、分页等
 * 继承ServiceImpl，自动获得基础CRUD方法
 */
@Slf4j
@Service("myBatisPlusExampleService")
public class MyBatisPlusExampleService extends ServiceImpl<UserPlusMapper, UserPlus> {

    /**
     * MyBatis Plus插入用户 - 无需编写SQL，自动生成
     */
    @Transactional
    public UserPlus createUser(String username, String email, Integer age) {
        UserPlus user = new UserPlus();
        user.setUsername(username);
        user.setEmail(email);
        user.setAge(age);
        // 注意：createTime和updateTime如果配置了自动填充，不需要手动设置

        boolean result = this.save(user);
        log.info("[MyBatis Plus] 插入用户结果: {}, 用户ID: {}", result, user.getId());
        return user;
    }

    /**
     * MyBatis Plus根据ID查询 - 无需编写SQL
     */
    public UserPlus getUserById(Long id) {
        UserPlus user = this.getById(id);
        log.info("[MyBatis Plus] 查询用户ID: {}, 结果: {}", id, user);
        return user;
    }

    /**
     * MyBatis Plus更新用户 - 无需编写SQL
     */
    @Transactional
    public UserPlus updateUser(Long id, String username, String email, Integer age) {
        UserPlus user = this.getById(id);
        if (user != null) {
            user.setUsername(username);
            user.setEmail(email);
            user.setAge(age);
            // updateTime会自动填充（如果配置了自动填充）
            boolean result = this.updateById(user);
            log.info("[MyBatis Plus] 更新用户结果: {}", result);
            return this.getById(id);
        }
        return null;
    }

    /**
     * MyBatis Plus查询所有用户 - 无需编写SQL
     */
    public List<UserPlus> getAllUsers() {
        List<UserPlus> users = this.list();
        log.info("[MyBatis Plus] 查询所有用户，数量: {}", users.size());
        return users;
    }

    /**
     * MyBatis Plus条件查询 - 使用QueryWrapper（字符串方式）
     */
    public List<UserPlus> getUsersByAge(Integer age) {
        QueryWrapper<UserPlus> wrapper = new QueryWrapper<>();
        wrapper.eq("age", age);
        List<UserPlus> users = this.list(wrapper);
        log.info("[MyBatis Plus] 根据年龄查询: {}, 结果数量: {}", age, users.size());
        return users;
    }

    /**
     * MyBatis Plus条件查询 - 使用LambdaQueryWrapper（类型安全）
     */
    public UserPlus getUserByUsername(String username) {
        LambdaQueryWrapper<UserPlus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlus::getUsername, username);
        UserPlus user = this.getOne(wrapper);
        log.info("[MyBatis Plus] 根据用户名查询: {}, 结果: {}", username, user);
        return user;
    }

    /**
     * MyBatis Plus复杂条件查询 - 多条件组合
     */
    public List<UserPlus> getUsersByConditions(String username, Integer minAge, Integer maxAge) {
        LambdaQueryWrapper<UserPlus> wrapper = new LambdaQueryWrapper<>();
        if (username != null) {
            wrapper.like(UserPlus::getUsername, username);  // 模糊查询
        }
        if (minAge != null) {
            wrapper.ge(UserPlus::getAge, minAge);  // 大于等于
        }
        if (maxAge != null) {
            wrapper.le(UserPlus::getAge, maxAge);  // 小于等于
        }
        wrapper.orderByDesc(UserPlus::getCreateTime);  // 按创建时间倒序
        
        List<UserPlus> users = this.list(wrapper);
        log.info("[MyBatis Plus] 复杂条件查询，结果数量: {}", users.size());
        return users;
    }

    /**
     * MyBatis Plus分页查询 - 内置分页插件
     */
    public IPage<UserPlus> getUsersByPage(int current, int size) {
        Page<UserPlus> page = new Page<>(current, size);
        IPage<UserPlus> result = this.page(page);
        log.info("[MyBatis Plus] 分页查询 - 当前页: {}, 每页大小: {}, 总数: {}, 总页数: {}",
                current, size, result.getTotal(), result.getPages());
        return result;
    }

    /**
     * MyBatis Plus分页条件查询
     */
    public IPage<UserPlus> getUsersByPageWithCondition(int current, int size, Integer minAge) {
        Page<UserPlus> page = new Page<>(current, size);
        LambdaQueryWrapper<UserPlus> wrapper = new LambdaQueryWrapper<>();
        if (minAge != null) {
            wrapper.ge(UserPlus::getAge, minAge);
        }
        wrapper.orderByDesc(UserPlus::getCreateTime);
        
        IPage<UserPlus> result = this.page(page, wrapper);
        log.info("[MyBatis Plus] 分页条件查询 - 总数: {}, 当前页数据: {}", 
                result.getTotal(), result.getRecords().size());
        return result;
    }

    /**
     * MyBatis Plus批量更新 - 使用UpdateWrapper
     */
    @Transactional
    public boolean batchUpdateAge(Integer oldAge, Integer newAge) {
        UpdateWrapper<UserPlus> wrapper = new UpdateWrapper<>();
        wrapper.eq("age", oldAge);
        wrapper.set("age", newAge);
        wrapper.set("update_time", LocalDateTime.now());
        
        boolean result = this.update(wrapper);
        log.info("[MyBatis Plus] 批量更新年龄: {} -> {}, 结果: {}", oldAge, newAge, result);
        return result;
    }

    /**
     * MyBatis Plus批量更新 - 使用LambdaUpdateWrapper（类型安全）
     */
    @Transactional
    public boolean batchUpdateAgeByLambda(Integer oldAge, Integer newAge) {
        LambdaUpdateWrapper<UserPlus> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserPlus::getAge, oldAge)
                .set(UserPlus::getAge, newAge);
        
        boolean result = this.update(wrapper);
        log.info("[MyBatis Plus] Lambda批量更新年龄: {} -> {}, 结果: {}", oldAge, newAge, result);
        return result;
    }

    /**
     * MyBatis Plus批量插入
     */
    @Transactional
    public boolean batchInsertUsers(List<UserPlus> users) {
        boolean result = this.saveBatch(users);
        log.info("[MyBatis Plus] 批量插入用户数量: {}, 结果: {}", users.size(), result);
        return result;
    }

    /**
     * MyBatis Plus批量删除
     */
    @Transactional
    public boolean batchDeleteUsers(List<Long> ids) {
        boolean result = this.removeByIds(ids);
        log.info("[MyBatis Plus] 批量删除用户IDs: {}, 结果: {}", ids, result);
        return result;
    }

    /**
     * MyBatis Plus统计查询
     */
    public long countByAge(Integer age) {
        LambdaQueryWrapper<UserPlus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlus::getAge, age);
        long count = this.count(wrapper);
        log.info("[MyBatis Plus] 统计年龄为{}的用户数量: {}", age, count);
        return count;
    }

    /**
     * MyBatis Plus根据ID删除
     */
    @Transactional
    public boolean deleteUser(Long id) {
        boolean result = this.removeById(id);
        log.info("[MyBatis Plus] 删除用户ID: {}, 结果: {}", id, result);
        return result;
    }

    /**
     * MyBatis Plus是否存在查询
     */
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<UserPlus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlus::getUsername, username);
        long count = this.count(wrapper);
        boolean exists = count > 0;
        log.info("[MyBatis Plus] 用户名{}是否存在: {}", username, exists);
        return exists;
    }
}


```


#### Short summary: 

empty definition using pc, found symbol in pc: com/baomidou/mybatisplus/core/metadata/IPage#