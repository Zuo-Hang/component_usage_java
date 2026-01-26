package com.example.mongodb.repository;

import com.example.mongodb.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户Repository
 * 继承MongoRepository，自动提供CRUD方法
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * 根据用户名查找（方法名自动生成查询）
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据年龄范围查找
     */
    List<User> findByAgeBetween(Integer minAge, Integer maxAge);

    /**
     * 根据城市查找
     */
    @Query("{ 'address.city': ?0 }")
    List<User> findByCity(String city);

    /**
     * 根据标签查找（包含指定标签）
     */
    @Query("{ 'tags': { $in: ?0 } }")
    List<User> findByTags(List<String> tags);

    /**
     * 根据用户名模糊查询
     */
    @Query("{ 'username': { $regex: ?0, $options: 'i' } }")
    List<User> findByUsernameLike(String username);

    /**
     * 统计年龄大于指定值的用户数
     */
    long countByAgeGreaterThan(Integer age);
}
