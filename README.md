# Eureka 核心源码学习与 Debug 仓库

本项目用于深入分析 Spring Cloud Netflix Eureka Server 的核心源码，通过调试与多实例配置理解其服务注册、发现及集群同步机制。

---

## 核心组件分析

### 1. **Eureka Server 自动配置**
- **类名**: `EurekaServerAutoConfiguration`
- **作用**: 自动配置 Eureka Server 的基础组件（如 `EurekaServerContext`、`PeerAwareInstanceRegistry`）。
- **关键逻辑**:
  - 初始化 `PeerEurekaNodes` 管理集群节点。
  - 注册 `EurekaServerBootstrap` 用于启动时的数据同步。

### 2. **服务注册与同步**
- **类名**: `PeerAwareInstanceRegistryImpl`
- **方法**: `register()`, `replicateToPeers()`
- **源码片段**:
  ```java
  // PeerAwareInstanceRegistryImpl.java
  public void register(InstanceInfo info, boolean isReplication) {
      super.register(info, isReplication);
      if (!isReplication) {
          replicateToPeers(Action.Register, info.getAppName(), info.getId(), info, null, isReplication);
      }
  }
  ```
说明: 服务注册时，若非复制请求，则通过 replicateToPeers 将注册信息同步到其他 Eureka 节点。

### 3. 集群节点管理
类名: PeerEurekaNodes

方法: updatePeerEurekaNodes()

作用: 动态更新集群节点列表，维护节点间心跳通信。

## 关键配置说明

### 多实例配置

- **`application-euk1.yml`/`application-euk2.yml`**:
  配置两个 Eureka Server 实例，互相注册形成集群。

  ```yaml
  server:
    port: 8761  # euk2 使用 8762
  eureka:
    instance:
      hostname: localhost
    client:
      serviceUrl:
        defaultZone: http://localhost:8762/eureka/  # euk2 指向 euk1
      fetch-registry: true
      register-with-eureka: true
  ```

### 全局配置

- **`application.yml`**:
  关闭自我保护模式（便于调试）并调整心跳检测间隔：

  ```yaml
  eureka:
    server:
      enable-self-preservation: false  # 关闭自我保护
      eviction-interval-timer-in-ms: 3000  # 清理间隔
    client:
      register-with-eureka: false  # 单机模式下不注册自身
  ```


## 学习心得与 Debug 实践

### 1. **服务注册流程**

- **Debug 入口**: `InstanceRegistry.register()` 方法。
- **观察点**: 注册信息如何通过 `replicateToPeers` 同步到其他节点，验证 HTTP 调用日志。

### 2. **集群数据一致性**

- **问题**: 节点间数据同步延迟导致短暂不一致。
- **解决**: 调整 `PeerEurekaNodes.heartbeatIntervalMs` 缩短同步间隔。

### 3. **自我保护机制**

- **源码分析**: `AbstractInstanceRegistry#evict()` 方法实现服务剔除逻辑。
- **触发条件**: 当心跳丢失比例超过阈值时，激活保护模式并停止剔除服务。

### 4. **CAP 特性验证**

- **结论**: Eureka 遵循 AP 设计，允许短暂数据不一致，优先保证可用性。
- **验证方式**: 关闭一个节点后，观察另一节点是否继续提供服务发现。

------

## 项目结构

复制

```
src
├── main
│   ├── java/com/benjamin/eurekastudy
│   │   └── EurekaStudyApplication.java  # 启动类
│   └── resources
│       ├── application-euk1.yml        # Eureka 实例 1 配置
│       ├── application-euk2.yml        # Eureka 实例 2 配置
│       └── application.yml             # 全局配置
├── test                                # 测试用例（可添加注册/发现测试）
├── pom.xml                             # 依赖管理（spring-cloud-starter-netflix-eureka-server）
└── ...
```

------

## 运行说明

1. 启动两个 Eureka 实例：

   bash

   复制

   ```
   java -jar target/eureka-study.jar --spring.profiles.active=euk1
   java -jar target/eureka-study.jar --spring.profiles.active=euk2
   ```

2. 访问控制台：

   - Euk1: [http://localhost:8761](http://localhost:8761/)
   - Euk2: [http://localhost:8762](http://localhost:8762/)

通过 Debug 断点可深入跟踪注册、续约及集群同步流程。

复制

```
此 README 涵盖了源码核心逻辑、配置说明及调试经验，帮助读者快速理解 Eureka Server 的工作原理。
```
