# MongoDB 示例模块

本模块演示如何使用 MongoDB 文档数据库，包括 CRUD 操作、查询、聚合等功能。

## 功能特性

### 1. 基本CRUD操作
- ✅ 创建文档
- ✅ 查询文档（ID、条件查询）
- ✅ 更新文档
- ✅ 删除文档

### 2. 查询功能
- ✅ 方法名自动生成查询
- ✅ 自定义查询（@Query）
- ✅ 分页查询
- ✅ 排序查询
- ✅ 模糊查询

### 3. 聚合查询
- ✅ 按字段分组统计
- ✅ 计算平均值
- ✅ 年龄分布统计
- ✅ 综合统计信息

### 4. 高级功能
- ✅ 嵌套文档支持
- ✅ 数组字段操作
- ✅ 索引管理（唯一索引）
- ✅ 使用 MongoTemplate 进行复杂操作

## 快速开始

### 1. 启动MongoDB服务

使用 Docker Compose 启动 MongoDB：

```bash
docker-compose up -d mongodb
```

MongoDB 连接信息：
- 地址：`localhost:27017`
- 数据库：`test_db`（自动创建）

### 2. 启动应用

```bash
cd mongodb-example
mvn spring-boot:run
```

### 3. 测试API

```bash
# 创建用户
curl -X POST http://localhost:8087/mongodb/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "age": 25,
    "address": {
      "city": "Beijing",
      "street": "Main St",
      "zipCode": "100000"
    },
    "tags": ["java", "spring"]
  }'
```

## API 接口

### 基本CRUD操作

#### 1. 创建用户
```bash
POST /mongodb/users
Content-Type: application/json

{
  "username": "john",
  "email": "john@example.com",
  "age": 25,
  "address": {
    "city": "Beijing",
    "street": "Main St",
    "zipCode": "100000"
  },
  "tags": ["java", "spring"]
}
```

响应：
```json
{
  "success": true,
  "user": {
    "id": "65a1b2c3d4e5f6g7h8i9j0k1",
    "username": "john",
    "email": "john@example.com",
    "age": 25,
    "address": {
      "city": "Beijing",
      "street": "Main St",
      "zipCode": "100000"
    },
    "tags": ["java", "spring"],
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "message": "用户创建成功"
}
```

#### 2. 根据ID获取用户
```bash
GET /mongodb/users/{id}
```

#### 3. 更新用户
```bash
PUT /mongodb/users/{id}
Content-Type: application/json

{
  "email": "newemail@example.com",
  "age": 26
}
```

#### 4. 删除用户
```bash
DELETE /mongodb/users/{id}
```

#### 5. 获取所有用户
```bash
GET /mongodb/users
```

### 查询操作

#### 1. 根据用户名查找
```bash
GET /mongodb/users/username/{username}
```

#### 2. 根据年龄范围查找
```bash
GET /mongodb/users/age?min=18&max=30
```

#### 3. 根据城市查找
```bash
GET /mongodb/users/city/{city}
```

#### 4. 根据标签查找
```bash
GET /mongodb/users/tags?tags=java&tags=spring
```

#### 5. 用户名模糊查询
```bash
GET /mongodb/users/search?username=john
```

#### 6. 分页查询
```bash
GET /mongodb/users/page?page=0&size=10&sortBy=username
```

响应：
```json
{
  "success": true,
  "users": [...],
  "totalElements": 100,
  "totalPages": 10,
  "currentPage": 0,
  "pageSize": 10
}
```

### 聚合查询

#### 1. 按城市统计用户数
```bash
GET /mongodb/users/stats/city
```

响应：
```json
{
  "success": true,
  "stats": [
    {"city": "Beijing", "count": 50},
    {"city": "Shanghai", "count": 30}
  ]
}
```

#### 2. 获取平均年龄
```bash
GET /mongodb/users/stats/avg-age
```

#### 3. 按年龄分组统计
```bash
GET /mongodb/users/stats/age-distribution
```

#### 4. 获取统计信息
```bash
GET /mongodb/users/stats
```

### 更新操作

#### 1. 更新用户字段
```bash
PATCH /mongodb/users/{id}/field?field=age&value=25
```

#### 2. 添加标签
```bash
POST /mongodb/users/{id}/tags?tag=python
```

## 配置说明

### application.yml 配置项

```yaml
spring:
  data:
    mongodb:
      # 方式1：使用URI（推荐）
      uri: mongodb://localhost:27017/test_db
      
      # 方式2：分别配置
      # host: localhost
      # port: 27017
      # database: test_db
      # username: admin
      # password: password
      # authentication-database: admin
```

### MongoDB URI格式

```
mongodb://[username:password@]host[:port][/database][?options]
```

示例：
- `mongodb://localhost:27017/test_db`
- `mongodb://admin:password@localhost:27017/test_db?authSource=admin`
- `mongodb://localhost:27017/test_db?maxPoolSize=50&minPoolSize=5`

## 使用示例

### 1. Repository方法

```java
@Autowired
private UserRepository userRepository;

// 根据用户名查找
Optional<User> user = userRepository.findByUsername("john");

// 根据年龄范围查找
List<User> users = userRepository.findByAgeBetween(18, 30);

// 根据城市查找（自定义查询）
List<User> users = userRepository.findByCity("Beijing");
```

### 2. MongoTemplate操作

```java
@Autowired
private MongoTemplate mongoTemplate;

// 自定义查询
Query query = new Query(Criteria.where("age").gte(18));
List<User> users = mongoTemplate.find(query, User.class);

// 更新部分字段
Update update = new Update().set("age", 25);
mongoTemplate.updateFirst(query, update, User.class);
```

### 3. 聚合查询

```java
// 按城市统计用户数
Aggregation aggregation = newAggregation(
    group("address.city").count().as("count"),
    sort(Sort.Direction.DESC, "count")
);
AggregationResults<Map> results = mongoTemplate.aggregate(
    aggregation, "users", Map.class);
```

## 核心概念

### 1. 文档（Document）

- MongoDB 存储的基本单位
- 类似于 JSON 对象
- 使用 BSON 格式存储

### 2. 集合（Collection）

- 类似于关系数据库中的表
- 不需要预定义结构
- 可以存储不同结构的文档

### 3. 数据库（Database）

- 包含多个集合
- 类似于关系数据库中的数据库

### 4. 索引（Index）

- 提高查询性能
- 支持唯一索引、复合索引等
- 使用 `@Indexed` 注解定义

### 5. 聚合管道（Aggregation Pipeline）

- 用于复杂的数据处理
- 支持多个阶段：`$match`、`$group`、`$sort` 等
- 类似于 SQL 的 GROUP BY

## 最佳实践

### 1. 文档设计

- ✅ 嵌入 vs 引用：小文档嵌入，大文档引用
- ✅ 避免过深的嵌套（建议不超过3-4层）
- ✅ 使用有意义的字段名

### 2. 索引策略

- ✅ 为常用查询字段创建索引
- ✅ 使用复合索引优化多字段查询
- ✅ 定期分析索引使用情况

### 3. 查询优化

- ✅ 使用投影减少数据传输
- ✅ 使用分页避免大量数据查询
- ✅ 合理使用聚合管道

### 4. 数据一致性

- ✅ 使用事务（MongoDB 4.0+）
- ✅ 合理使用唯一索引
- ✅ 考虑数据验证规则

## 常见问题

### Q1: 连接MongoDB失败？

**A**: 检查以下几点：
1. MongoDB服务是否启动
2. 连接URI是否正确
3. 网络是否连通
4. 认证信息是否正确

### Q2: 查询性能慢？

**A**: 可以：
1. 创建合适的索引
2. 使用投影减少字段
3. 使用分页查询
4. 分析慢查询日志

### Q3: 如何处理嵌套文档？

**A**: 
- 使用内部类定义嵌套文档
- 使用 `@Field` 注解指定字段名
- 查询时使用点号访问嵌套字段（如：`address.city`）

## 参考资源

- [MongoDB 官方文档](https://www.mongodb.com/docs/)
- [Spring Data MongoDB 文档](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
- [MongoDB Compass](https://www.mongodb.com/products/compass) - 可视化工具
