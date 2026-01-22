package com.example.mysql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mysql.model.UserPlus;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis Plus Mapper接口
 * 继承BaseMapper，自动获得基础CRUD方法，无需编写SQL
 */
@Mapper
public interface UserPlusMapper extends BaseMapper<UserPlus> {
    
    // 基础CRUD方法已由BaseMapper提供：
    // - insert(entity) - 插入
    // - deleteById(id) - 根据ID删除
    // - updateById(entity) - 根据ID更新
    // - selectById(id) - 根据ID查询
    // - selectList(wrapper) - 查询列表
    // - selectCount(wrapper) - 查询数量
    // 等等...
    
    // 如果需要自定义方法，可以在这里添加
    // @Select("SELECT * FROM user WHERE username = #{username}")
    // UserPlus selectByUsername(String username);
}

