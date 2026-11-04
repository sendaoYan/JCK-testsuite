# java.util.logging.LogRecord 测试方案

白盒结构断言在 `LogRecordTest.testWhiteBox()`，黑盒行为断言在 `LogRecordTest.testBlackBox()`。

## 1. API 功能

- **构造器**：`LogRecord(Level, String)`。
- **字段访问器**：level/message/loggerName/millis/sequenceNumber/threadID/sourceClassName/sourceMethodName/parameters/thrown/resourceBundle/resourceBundleName 的 get/set。

## 2. Java 用户如何使用

- 手工构造：`new LogRecord(Level.INFO, "a={0}")`。
- 填充：`r.setLoggerName(...); r.setMillis(now); r.setParameters(new Object[]{x});`
- 由 Logger.log 自动填充后交 Handler/Formatter 消费。

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/logging/LogRecordThreadIdTest.java:50-52`：`new LogRecord(Level.INFO, "record")` 三连。
- `test/jdk/java/util/logging/SerializeLogRecordTest.java:185`：`new LogRecord(Level.INFO, "Java Version: {0}")`。
- `test/jdk/java/util/logging/CheckZombieLockTest.java:138`：`handler.publish(new LogRecord(Level.INFO, handler.toString()))`。

## 4. 测试用例设计 + 自动化实现

- **正常**：所有 get/set 往返；固定 epoch `setMillis`。
- **组合**：两个新记录的 sequenceNumber 严格递增；`setSequenceNumber` 显式覆盖；`setThreadID` 往返。
- **边界**：ResourceBundle 与 bundleName 独立设置；toString 非空。
- **健壮性**：固定 epoch、无墙钟依赖。
