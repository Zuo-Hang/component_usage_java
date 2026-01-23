# 组件实现路线图

本文档列出了适合和不适合添加到当前项目的组件，作为后续开发的参考指南。

## 项目定位

**当前项目定位**：Java 组件使用示例集合
- 展示如何**使用**各种组件/中间件
- 通过 Spring Boot Web 应用演示
- 通过 REST API 提供调用示例
- 使用 Docker Compose 提供依赖服务

## ✅ 适合实现的组件

### 一、数据存储组件

#### 1. 关系型数据库
- ✅ **MySQL** - 已实现
- ✅ **PostgreSQL** - 推荐实现
- ✅ **Oracle** - 可选（商业数据库）
- ✅ **SQL Server** - 可选（商业数据库）
- ✅ **H2** - 推荐实现（内存数据库，适合测试）

**实现方式**：
- JDBC Template
- MyBatis
- MyBatis Plus
- JPA/Hibernate

#### 2. NoSQL 数据库
- ✅ **MongoDB** - 强烈推荐
  - 文档数据库
  - Spring Data MongoDB
  - 适合演示 CRUD、聚合查询

- ✅ **Redis** - 已实现
  - 键值存储
  - 已支持 Jedis、Lettuce、Redisson

- ✅ **Elasticsearch** - 推荐实现
  - 搜索引擎
  - Spring Data Elasticsearch
  - 适合演示搜索、聚合

- ✅ **Cassandra** - 可选
  - 列式数据库
  - Spring Data Cassandra

- ✅ **CouchDB** - 可选
  - 文档数据库

#### 3. 图数据库
- ✅ **Neo4j** - 可选
  - 图数据库
  - Spring Data Neo4j

### 二、消息中间件

#### 1. 消息队列
- ✅ **RocketMQ** - 已实现（mq-rocketmq-example 模块）
- ✅ **Kafka** - 强烈推荐（建议创建 mq-kafka-example 模块）
  - 分布式流处理平台
  - Spring Kafka
  - 适合演示生产者、消费者、流处理

- ✅ **RabbitMQ** - 推荐实现（建议创建 mq-rabbitmq-example 模块）
  - AMQP 消息队列
  - Spring AMQP
  - 适合演示队列、交换机、路由

- ✅ **ActiveMQ** - 可选（建议创建 mq-activemq-example 模块）
  - JMS 消息队列
  - Spring JMS

- ✅ **Pulsar** - 可选（建议创建 mq-pulsar-example 模块）
  - 云原生消息系统

#### 2. 事件流
- ✅ **Redis Streams** - 可选
  - Redis 5.0+ 的流处理功能

### 三、协调服务

- ✅ **ZooKeeper** - 已实现
- ✅ **Etcd** - 推荐实现
  - 分布式键值存储
  - 服务发现、配置管理
  - etcd4j 客户端

- ✅ **Consul** - 可选
  - 服务发现和配置管理
  - Consul Java Client

### 四、缓存组件

#### 1. 本地缓存
- ✅ **Caffeine** - 强烈推荐
  - 高性能本地缓存
  - Spring Cache 集成

- ✅ **Guava Cache** - 推荐实现
  - Google Guava 缓存
  - Spring Cache 集成

- ✅ **Ehcache** - 可选
  - Java 缓存框架

#### 2. 分布式缓存
- ✅ **Redis** - 已实现（也可作为分布式缓存）

### 五、配置中心

- ✅ **Nacos** - 强烈推荐
  - 服务发现和配置管理
  - Spring Cloud Alibaba
  - 适合演示配置管理、服务注册

- ✅ **Apollo** - 推荐实现
  - 携程开源的配置中心
  - Apollo Java Client

- ✅ **Spring Cloud Config** - 可选
  - Spring Cloud 配置中心

### 六、服务注册与发现

- ✅ **Eureka** - 可选
  - Netflix 服务注册中心
  - Spring Cloud Netflix

- ✅ **Nacos** - 见配置中心（也支持服务注册）

### 七、对象存储

- ✅ **MinIO** - 推荐实现
  - 对象存储服务
  - S3 兼容 API
  - 适合演示文件上传、下载

- ✅ **OSS（阿里云）** - 可选
  - 阿里云对象存储

- ✅ **S3（AWS）** - 可选
  - AWS 对象存储

### 八、搜索引擎

- ✅ **Elasticsearch** - 见 NoSQL 数据库
- ✅ **Solr** - 可选
  - Apache Solr
  - Spring Data Solr

### 九、任务调度

- ✅ **Quartz** - 推荐实现
  - 任务调度框架
  - Spring Boot Quartz
  - 适合演示定时任务、集群调度

- ✅ **XXL-JOB** - 可选
  - 分布式任务调度平台

- ✅ **Elastic-Job** - 可选
  - 分布式任务调度框架

### 十、限流降级

- ✅ **Sentinel** - 强烈推荐
  - 流量控制、熔断降级
  - Spring Cloud Alibaba
  - 适合演示限流、熔断、降级

- ✅ **Hystrix** - 可选（已停止维护）
  - Netflix 熔断器

### 十一、API 网关

- ✅ **Spring Cloud Gateway** - 可选
  - Spring Cloud 网关
  - 适合演示路由、过滤、限流

- ✅ **Kong** - 可选
  - 云原生 API 网关

### 十二、日志组件

- ✅ **Logback** - 可选（Spring Boot 默认）
- ✅ **Log4j2** - 可选
- ✅ **ELK Stack** - 可选
  - Elasticsearch + Logstash + Kibana
  - 适合演示日志收集和分析

### 十三、监控组件

- ✅ **Prometheus** - 推荐实现
  - 监控和告警
  - Spring Boot Actuator 集成

- ✅ **Micrometer** - 可选
  - 指标收集框架

### 十四、序列化组件

- ✅ **Jackson** - 可选（Spring Boot 默认）
- ✅ **Gson** - 可选
- ✅ **Fastjson** - 可选（注意安全）

### 十五、HTTP 客户端

- ✅ **RestTemplate** - 可选（Spring 默认）
- ✅ **WebClient** - 推荐实现
  - Spring WebFlux 响应式客户端

- ✅ **OkHttp** - 可选
- ✅ **Apache HttpClient** - 可选

### 十六、模板引擎

- ✅ **Thymeleaf** - 可选
  - Spring Boot 默认模板引擎

- ✅ **Freemarker** - 可选
- ✅ **Velocity** - 可选

## ❌ 不适合实现的组件

### 一、计算框架

- ❌ **Apache Spark**
  - 原因：分布式计算框架，需要集群环境，提交作业运行
  - 建议：创建独立的 Spark 学习项目

- ❌ **Apache Flink**
  - 原因：流处理框架，需要集群环境，提交作业运行
  - 建议：创建独立的 Flink 学习项目

- ❌ **Apache Storm**
  - 原因：实时计算框架，需要集群环境

- ❌ **Apache Beam**
  - 原因：统一批处理和流处理框架

### 二、大数据存储

- ❌ **Hadoop HDFS**
  - 原因：分布式文件系统，需要集群环境
  - 建议：作为大数据生态的一部分单独学习

- ❌ **Apache HBase**
  - 原因：分布式列式数据库，需要 Hadoop 集群
  - 建议：作为大数据生态的一部分单独学习

- ❌ **Apache Kudu**
  - 原因：列式存储系统，需要集群环境

### 三、数据仓库

- ❌ **Apache Hive**
  - 原因：数据仓库工具，需要 Hadoop 环境

- ❌ **Apache Impala**
  - 原因：SQL 查询引擎，需要集群环境

### 四、机器学习框架

- ❌ **TensorFlow**
  - 原因：机器学习框架，不是组件库
  - 建议：作为独立的 ML 项目学习

- ❌ **PyTorch**
  - 原因：机器学习框架，不是组件库

- ❌ **MLlib（Spark）**
  - 原因：Spark 的机器学习库，需要 Spark 集群

### 五、工作流引擎

- ❌ **Apache Airflow**
  - 原因：工作流调度平台，需要独立部署
  - 建议：作为独立的 DevOps 工具学习

- ❌ **Azkaban**
  - 原因：工作流调度系统

### 六、容器编排

- ❌ **Kubernetes**
  - 原因：容器编排平台，不是 Java 组件
  - 建议：作为基础设施单独学习

- ❌ **Docker Swarm**
  - 原因：容器编排工具，不是 Java 组件

## 📊 优先级建议

### 高优先级（强烈推荐）

1. **Kafka** - 消息队列，使用广泛（建议创建 mq-kafka-example 模块）
2. **MongoDB** - NoSQL 数据库，文档存储
3. **Elasticsearch** - 搜索引擎，全文检索
4. **RabbitMQ** - 消息队列，AMQP 协议（建议创建 mq-rabbitmq-example 模块）
5. **Nacos** - 配置中心和服务注册
6. **Sentinel** - 限流降级，微服务必备
7. **Caffeine** - 本地缓存，性能优秀
8. **Quartz** - 任务调度，定时任务
9. **MinIO** - 对象存储，S3 兼容

### 中优先级（推荐）

1. **PostgreSQL** - 关系型数据库
2. **Etcd** - 分布式协调服务
3. **Apollo** - 配置中心
4. **WebClient** - 响应式 HTTP 客户端
5. **Prometheus** - 监控指标

### 低优先级（可选）

1. **Cassandra** - 列式数据库
2. **Neo4j** - 图数据库
3. **ActiveMQ** - JMS 消息队列
4. **Guava Cache** - 本地缓存
5. **XXL-JOB** - 任务调度

## 🎯 实现建议

### 实现原则

1. **保持一致性**
   - 每个模块都是独立的 Spring Boot 应用
   - 通过 REST API 提供调用示例
   - 使用 Docker Compose 提供依赖服务

2. **代码结构**
   - 参考现有模块的结构（redis-example、mysql-example）
   - 提供工具类封装（如 ZooKeeperUtil）
   - 提供 Service 层和 Controller 层

3. **文档完善**
   - 每个模块都有 README.md
   - 提供快速开始指南
   - 提供 API 使用示例

4. **配置管理**
   - 使用 application.yml 配置
   - 支持 Docker Compose 环境变量

### 实现步骤

1. **在根 pom.xml 添加模块**
   ```xml
   <modules>
       <module>new-component-example</module>
   </modules>
   ```

2. **创建模块目录结构**
   ```
   new-component-example/
   ├── pom.xml
   ├── README.md
   └── src/main/
       ├── java/com/example/newcomponent/
       │   ├── config/
       │   ├── controller/
       │   ├── service/
       │   ├── util/
       │   └── NewComponentExampleApplication.java
       └── resources/
           └── application.yml
   ```

3. **添加 Docker 服务**
   - 在 docker-compose.yml 中添加依赖服务
   - 配置网络和端口映射

4. **编写代码示例**
   - 配置类
   - 工具类（如需要）
   - Service 层
   - Controller 层

5. **编写文档**
   - README.md
   - API 使用示例
   - 配置说明

## 📝 注意事项

1. **版本兼容性**
   - 确保组件版本与 Spring Boot 3.2.0 兼容
   - 确保组件版本与 Java 21 兼容

2. **依赖管理**
   - 在父 pom.xml 的 dependencyManagement 中统一管理版本
   - 避免版本冲突

3. **端口管理**
   - 每个模块使用不同的端口（8080、8081、8082...）
   - 在 README 中说明端口配置

4. **Docker 服务**
   - 确保 Docker 服务名称唯一
   - 使用统一的网络（component-network）
   - 配置健康检查

5. **代码质量**
   - 遵循现有代码风格
   - 添加必要的注释
   - 提供完整的异常处理

## 🔄 更新记录

- 2026-01-22：初始版本，列出适合和不适合的组件

---

**说明**：本文档会根据项目发展持续更新，建议定期查看最新版本。
