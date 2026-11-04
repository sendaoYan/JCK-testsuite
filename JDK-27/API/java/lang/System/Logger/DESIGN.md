# System.Logger — 设计方案

## 1. API 功能
`System.Logger`（interface）是 JDK 平台的日志门面：
- 查询：`getName()`、`isLoggable(Level)`
- 记录：多个 `log(Level, ...)` 重载（String/Supplier/Object/Throwable/varargs/ResourceBundle）

## 2. Java 用户如何使用
- `System.getLogger(name)` 获取命名 logger，按级别记录消息。
```java
System.Logger log = System.getLogger("com.example");
if (log.isLoggable(System.Logger.Level.DEBUG)) log.log(System.Logger.Level.DEBUG, "msg");
```

## 3. 开源代码实际用法调研
- JDK 内部（如 `jdk.internal` 组件）用 `System.Logger` 替代 `java.util.logging` 记录平台日志。
- 第三方通过 `System.LoggerFinder` 自定义后端（JDK `test/jdk/java/lang/System/Logger` 系列验证）。

## 4. 测试用例设计 + 自动化实现
白盒：`System.Logger.class.isInterface()`。
黑盒：`getLogger` 返回命名 logger；`isLoggable(INFO)`；六种 `log` 重载不抛；`getLogger(name, bundle)` 返回非空。
对应 `<Simple>Test.java` 黑盒部分。
