# java.util.logging.LogManager 测试方案

白盒结构断言在 `LogManagerTest.testWhiteBox()`，黑盒行为断言在 `LogManagerTest.testBlackBox()`。

## 1. API 功能

- **单例/构造**：`getLogManager()` 返回全局单例；protected 构造器（可子类化做隔离实例）。
- **配置**：`readConfiguration()`、`readConfiguration(InputStream)`、`getProperty(String)`。
- **logger 管理**：`getLogger(String)`、`getLoggerNames()`、`addLogger(Logger)`、`reset()`。
- **其它**：`checkAccess()`（有 SecurityManager 时校验，否则空操作）、`getLoggingMXBean()`。

## 2. Java 用户如何使用

- 全局：`LogManager.getLogManager()` 读取/更新配置。
- 隔离实例：`class T extends LogManager {}` 后 `new T()`，再 `readConfiguration(is)`（不污染全局）。
- 注册 logger：`lm.addLogger(logger); lm.getLogger(name); lm.getLoggerNames(); lm.reset();`

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/logging/LogManager/TestLoggerNames.java:137`：`LogManager.getLogManager().addLogger(test)`。
- `test/jdk/java/util/logging/LogManager/TestLoggerNames.java:145-146`：`Logger.getLogger("one.two.addMeAChild")` 等具名创建。
- `test/jdk/java/util/logging/TestLogConfigurationDeadLockWithConf.java:201`：`LogManager.getLogManager().readConfiguration()`。
- `test/jdk/java/util/logging/CustomLogManagerTest.java:48`：`Logger.getLogger(CustomLogManagerTest.class.getName())` —— 自定义 LogManager 场景。

## 4. 测试用例设计 + 自动化实现

- **全局**：`getLogManager()` 非空且幂等；`getLoggerNames()`/`getLoggingMXBean()` 非空；`checkAccess()` 空操作。
- **隔离实例**：子类实例化 + `readConfiguration(InputStream)` 读入 `.level` 与 logger 等级；`getProperty` 精确断言；`addLogger` 返回 true/重复 false；`getLogger` 命中/未知 null；`getLoggerNames` 含已注册 logger；`reset` 清属性与等级但保留 logger。
- **健壮性**：用隔离实例，绝不 reset 全局 LogManager；具名 logger 隔离。
