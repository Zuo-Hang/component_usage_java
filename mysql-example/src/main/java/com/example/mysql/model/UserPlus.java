package com.example.mysql.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MyBatis Plus用户实体类
 * 继承MyBatis Plus的字段填充、逻辑删除等功能
 */
@Data
@TableName("user")  // 指定表名
public class UserPlus {

    /**
     * 主键ID，使用数据库自增策略
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String email;
    private Integer age;

    /**
     * 字段填充策略：插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 字段填充策略：插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除字段（可选）
     * @TableLogic(value = "0", delval = "1")  // 0表示未删除，1表示已删除
     */
    // @TableLogic
    // private Integer deleted;
}

