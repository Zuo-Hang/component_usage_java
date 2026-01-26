package com.example.doris.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体
 * 
 * 用于演示多表JOIN场景
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long userId;
    private String username;
    private String email;
    private String city;
    private String country;
    private LocalDateTime registerTime;
    private Integer age;
    private String gender;
}
