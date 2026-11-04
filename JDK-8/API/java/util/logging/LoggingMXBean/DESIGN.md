# java.util.logging.LoggingMXBean 测试方案

白盒结构断言在 `LoggingMXBeanTest.testWhiteBox()`，黑盒行为断言在 `LoggingMXBeanTest.testBlackBox()`。

## 1. API 功能

- **接口**：`getLoggerNames()`、`getLoggerLevel(String)`、`setLoggerLevel(String,String)`、`getParentLoggerName(String)`。
- **现状**：JDK-9 起 `@Deprecated`，不再是 platform MXBean（不 extends PlatformManagedObject），推荐 `java.lang.management.PlatformLoggingMXBean`。仍可通过 `LogManager.getLoggingMXBean()` 获取。

## 2. Java 用户如何使用

- `LoggingMXBean mx = LogManager.getLoggingMXBean();`
- 读：`mx.getLoggerNames()`、`mx.getLoggerLevel("com.app")`（null 等级返回 `""`，未知 logger 返回 null）。
- 写：`mx.setLoggerLevel("com.app", "WARNING")`；`getParentLoggerName("com.app")` 返回父名。

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev`，逐条标注来源。

- `src/java.logging/share/classes/java/util/logging/Logging.java:59-130`：`Logging implements LoggingMXBean` 各方法实现（getLoggerNames/getLoggerLevel/setLoggerLevel/getParentLoggerName）。
- `src/java.logging/share/classes/java/util/logging/LogManager.java:1170`：`getLoggingMXBean()` 返回 `Logging.getInstance()`。
- `src/java.logging/share/classes/java/util/logging/LoggingMXBean.java:60-70`：`@Deprecated(since="9")` 说明。

## 4. 测试用例设计 + 自动化实现

- **正常**：`getLoggerNames()` 非空且含 "global"；对已存在 logger 的 level 读/写往返并反映到 `Logger.getLevel()`。
- **组合**：`setLoggerLevel(name, null)` → 等级空串 + Logger 等级 null；`getParentLoggerName` 返回父名。
- **边界/异常**：未知 logger 的 level/parent 返回 null；`setLoggerLevel(null,...)` 抛 NPE；未知 logger/未知 level 抛 IAE。
- **健壮性**：只操作具名 jck logger，只读全局，无全局污染。
