# 定时任务管理系统 (Scheduled Task Management System)

一个基于Spring Boot的企业级定时任务管理系统，提供完整的Web界面管理功能。

## ✨ 主要特性

- 🎯 **Web界面管理** - 直观的Web界面，支持任务的增删改查操作
- ⏰ **精确到秒的Cron表达式** - 支持标准Cron表达式，最高精度到秒级
- 🔄 **任务失败重试机制** - 可配置的重试次数，支持失败任务自动重试
- ⏱️ **任务超时强制终止** - 支持设置任务超时时间，超时自动终止
- 🔗 **任务依赖配置** - 支持任务间依赖关系，A任务完成后再执行B任务
- 📊 **执行历史追踪** - 完整记录任务执行历史，包括执行时间、状态、结果等
- 🎛️ **任务状态管理** - 支持任务启动、停止、暂停、恢复等状态控制
- 🛡️ **循环依赖检测** - 自动检测并防止任务间的循环依赖

## 🏗️ 技术架构

- **后端框架**: Spring Boot 3.2.0
- **调度引擎**: Spring Quartz
- **数据持久化**: Spring Data JPA + Hibernate
- **数据库**: H2 Database (可扩展至MySQL/PostgreSQL)
- **前端技术**: Thymeleaf + Bootstrap 5 + FontAwesome
- **构建工具**: Maven
- **Java版本**: 17+

## 🚀 快速开始

### 环境要求

- JDK 17或更高版本
- Maven 3.6+

### 运行应用

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd scheduled-task
   ```

2. **编译项目**
   ```bash
   mvn clean compile
   ```

3. **运行测试**
   ```bash
   mvn test
   ```

4. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

5. **访问应用**
   - Web界面: http://localhost:8080/web/tasks
   - H2控制台: http://localhost:8080/h2-console
   - REST API: http://localhost:8080/api/tasks

## 📱 使用说明

### Web界面操作

1. **任务列表页面** (`/web/tasks`)
   - 查看所有定时任务
   - 显示任务状态、Cron表达式、创建时间等信息
   - 支持任务的启动、停止、暂停、恢复操作

2. **创建任务** (`/web/tasks/new`)
   - 设置任务名称和描述
   - 配置Cron表达式（精确到秒）
   - 选择任务执行类
   - 设置任务参数（JSON格式）
   - 配置重试次数和超时时间

3. **任务详情页面** (`/web/tasks/{id}`)
   - 查看任务完整信息
   - 管理任务依赖关系
   - 查看任务执行历史
   - 执行任务操作（启动/停止/暂停/恢复/删除）

### Cron表达式示例

系统支持6位或7位的Cron表达式，格式为：`秒 分 时 日 月 星期 [年]`

- `0/30 * * * * ?` - 每30秒执行一次
- `0 0/5 * * * ?` - 每5分钟执行一次
- `0 0 * * * ?` - 每小时执行一次
- `0 0 0 * * ?` - 每天午夜执行一次
- `0 0 12 * * ?` - 每天中午12点执行
- `0 0 0 ? * MON` - 每周一午夜执行
- `0 0 0 1 * ?` - 每月1号午夜执行
- `0 0 0 1 1 ? *` - 每年1月1日执行

### REST API

系统提供完整的REST API接口：

#### 任务管理
- `GET /api/tasks` - 获取所有任务
- `GET /api/tasks/{id}` - 获取指定任务
- `POST /api/tasks` - 创建新任务
- `PUT /api/tasks/{id}` - 更新任务
- `DELETE /api/tasks/{id}` - 删除任务

#### 任务控制
- `POST /api/tasks/{id}/start` - 启动任务
- `POST /api/tasks/{id}/stop` - 停止任务
- `POST /api/tasks/{id}/pause` - 暂停任务
- `POST /api/tasks/{id}/resume` - 恢复任务

#### 执行历史
- `GET /api/tasks/{id}/executions` - 获取任务执行历史
- `GET /api/tasks/executions/running` - 获取正在运行的执行

#### 依赖管理
- `POST /api/dependencies?taskId={taskId}&dependentTaskId={dependentTaskId}` - 添加依赖
- `DELETE /api/dependencies/{id}` - 删除依赖
- `GET /api/dependencies/task/{taskId}` - 获取任务依赖
- `GET /api/dependencies/dependents/{taskId}` - 获取依赖该任务的其他任务

## 🔧 开发指南

### 自定义任务类

1. 继承 `BaseScheduledJob` 类
2. 实现 `executeJob` 方法
3. 在方法中编写具体的业务逻辑

```java
@Component
public class CustomJob extends BaseScheduledJob {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomJob.class);
    
    @Override
    protected String executeJob(JobExecutionContext context) throws Exception {
        String jobData = context.getJobDetail().getJobDataMap().getString("jobData");
        logger.info("执行自定义任务，参数: {}", jobData);
        
        // 检查是否被中断
        if (isInterrupted()) {
            return "任务被中断";
        }
        
        // 执行具体业务逻辑
        // ...
        
        return "任务执行成功";
    }
}
```

### 数据库配置

默认使用H2内存数据库，可通过修改 `application.yml` 切换到其他数据库：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/scheduled_task
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: password
```

### 配置说明

主要配置项说明：

```yaml
spring:
  quartz:
    job-store-type: memory  # 任务存储类型：memory/jdbc
    properties:
      org.quartz.scheduler.instanceName: ScheduledTaskScheduler
      org.quartz.threadPool.threadCount: 10  # 线程池大小

logging:
  level:
    com.eyesdawn.scheduledtask: DEBUG  # 日志级别
```

## 📊 系统监控

- 任务执行状态实时监控
- 任务执行历史记录
- 任务执行时间统计
- 失败任务重试记录
- 系统运行日志

## 🛠️ 故障排除

### 常见问题

1. **任务不执行**
   - 检查Cron表达式是否正确
   - 确认任务状态是否为"活跃"
   - 查看系统日志了解具体错误

2. **任务执行失败**
   - 检查任务类是否正确加载
   - 确认任务参数格式是否正确
   - 查看执行历史中的错误信息

3. **依赖任务不执行**
   - 确认依赖任务已完成
   - 检查是否存在循环依赖
   - 验证依赖关系配置

## 🤝 贡献指南

欢迎提交Issue和Pull Request来改进这个项目！

## 📄 许可证

本项目采用MIT许可证，详情请参见LICENSE文件。
