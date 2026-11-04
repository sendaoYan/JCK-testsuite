# java.util.logging.Filter 测试方案

白盒结构断言在 `FilterTest.testWhiteBox()`，黑盒行为断言在 `FilterTest.testBlackBox()`。

## 1. API 功能

- **函数式接口**：`boolean isLoggable(LogRecord record)`，单一抽象方法，标注 `@FunctionalInterface`。
- **用途**：挂在 `Handler`（`Handler.setFilter`）或 `Logger`（`Logger.setFilter`）上，判定一条 `LogRecord` 是否应被发布。

## 2. Java 用户如何使用

- lambda：`Filter f = r -> r.getLevel().intValue() >= Level.INFO.intValue();`
- 方法引用：`Filter f = this::shouldLog;`
- 与 Handler 组合：`handler.setFilter(r -> r.getMessage().startsWith("keep")); handler.publish(record);`

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源。

- `src/java.logging/share/classes/java/util/logging/Handler.java:323-330`：`isLoggable` 内部调用 `filter.isLoggable(record)` —— Handler 与 Filter 的组合点。
- `test/jdk/java/util/logging/IsLoggableHandlerTest.java:27`：`@summary Test that Handler.isLoggable(null) returns false` —— 官方测试 Filter/Handler 判空边界。
- `src/java.logging/share/classes/java/util/logging/Logger.java`（`setFilter`/`getFilter`）—— Logger 侧同样持有 Filter。

## 4. 测试用例设计 + 自动化实现

- **正常**：lambda 对 INFO/SEVERE/FINE 判 true/false；contains 谓词 true/false。
- **组合**：`Handler.setFilter` + `publish` 三条记录，断言被过滤后只保留两条且顺序正确。
- **边界**：空 lambda 均返回 true/false 的两态覆盖。
- **健壮性**：无全局状态、无墙钟、无文件/网络。
