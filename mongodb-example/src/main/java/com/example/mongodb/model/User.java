package com.example.mongodb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类
 * 演示MongoDB文档存储
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("username")
    private String username;

    @Field("email")
    private String email;

    @Field("age")
    private Integer age;

    @Field("address")
    private Address address;

    @Field("tags")
    private List<String> tags;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 地址嵌套文档
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        @Field("city")
        private String city;

        @Field("street")
        private String street;

        @Field("zip_code")
        private String zipCode;
    }
}
