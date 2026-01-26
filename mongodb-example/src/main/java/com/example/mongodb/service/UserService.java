package com.example.mongodb.service;

import com.example.mongodb.model.User;
import com.example.mongodb.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * 用户服务类
 * 演示MongoDB的各种操作
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    // ========== 基本CRUD操作 ==========

    /**
     * 创建用户
     */
    public User createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        log.info("创建用户成功: id={}, username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 更新用户
     */
    public User updateUser(String id, User user) {
        Optional<User> existing = userRepository.findById(id);
        if (existing.isPresent()) {
            User existingUser = existing.get();
            existingUser.setEmail(user.getEmail());
            existingUser.setAge(user.getAge());
            existingUser.setAddress(user.getAddress());
            existingUser.setTags(user.getTags());
            existingUser.setUpdatedAt(LocalDateTime.now());
            User updated = userRepository.save(existingUser);
            log.info("更新用户成功: id={}", id);
            return updated;
        }
        throw new RuntimeException("用户不存在: " + id);
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(String id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.info("删除用户成功: id={}", id);
            return true;
        }
        return false;
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ========== 查询操作 ==========

    /**
     * 根据年龄范围查找用户
     */
    public List<User> findByAgeRange(Integer minAge, Integer maxAge) {
        return userRepository.findByAgeBetween(minAge, maxAge);
    }

    /**
     * 根据城市查找用户
     */
    public List<User> findByCity(String city) {
        return userRepository.findByCity(city);
    }

    /**
     * 根据标签查找用户
     */
    public List<User> findByTags(List<String> tags) {
        return userRepository.findByTags(tags);
    }

    /**
     * 根据用户名模糊查询
     */
    public List<User> findByUsernameLike(String username) {
        return userRepository.findByUsernameLike(username);
    }

    /**
     * 分页查询
     */
    public Page<User> findUsersWithPagination(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return userRepository.findAll(pageable);
    }

    // ========== 使用MongoTemplate进行复杂查询 ==========

    /**
     * 使用MongoTemplate查询
     */
    public List<User> findUsersByCustomQuery(String city, Integer minAge) {
        Query query = new Query();
        if (city != null) {
            query.addCriteria(Criteria.where("address.city").is(city));
        }
        if (minAge != null) {
            query.addCriteria(Criteria.where("age").gte(minAge));
        }
        return mongoTemplate.find(query, User.class);
    }

    /**
     * 更新用户的部分字段
     */
    public boolean updateUserField(String id, String field, Object value) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set(field, value).set("updated_at", LocalDateTime.now());
        var result = mongoTemplate.updateFirst(query, update, User.class);
        return result.getModifiedCount() > 0;
    }

    /**
     * 添加标签
     */
    public boolean addTag(String id, String tag) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().addToSet("tags", tag).set("updated_at", LocalDateTime.now());
        var result = mongoTemplate.updateFirst(query, update, User.class);
        return result.getModifiedCount() > 0;
    }

    // ========== 聚合查询 ==========

    /**
     * 按城市统计用户数
     */
    public List<Map<String, Object>> countUsersByCity() {
        Aggregation aggregation = newAggregation(
                group("address.city").count().as("count"),
                project("count").and("city").previousOperation(),
                sort(Sort.Direction.DESC, "count")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(
                aggregation, "users", Map.class);

        List<Map<String, Object>> resultList = results.getMappedResults();
        log.info("按城市统计用户数: {}", resultList);
        return resultList;
    }

    /**
     * 计算平均年龄
     */
    public double getAverageAge() {
        Aggregation aggregation = newAggregation(
                group().avg("age").as("avgAge")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(
                aggregation, "users", Map.class);

        Map<String, Object> result = results.getUniqueMappedResult();
        if (result != null && result.get("avgAge") != null) {
            return ((Number) result.get("avgAge")).doubleValue();
        }
        return 0.0;
    }

    /**
     * 按年龄分组统计
     */
    public List<Map<String, Object>> groupByAgeRange() {
        Aggregation aggregation = newAggregation(
                project("age")
                        .andExpression("floor(age / 10) * 10").as("ageRange"),
                group("ageRange").count().as("count"),
                project("count").and("ageRange").previousOperation(),
                sort(Sort.Direction.ASC, "ageRange")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(
                aggregation, "users", Map.class);

        return results.getMappedResults();
    }

    /**
     * 统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("averageAge", getAverageAge());
        stats.put("usersByCity", countUsersByCity());
        stats.put("ageDistribution", groupByAgeRange());
        return stats;
    }
}
