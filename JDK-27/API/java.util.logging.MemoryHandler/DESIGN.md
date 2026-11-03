# java.util.logging.MemoryHandler 测试方案

白盒结构断言在 `MemoryHandlerTest.testWhiteBox()`，黑盒行为断言在 `MemoryHandlerTest.testBlackBox()`。

## 1. API 功能

- **继承**：`extends Handler`；构造器 `MemoryHandler()` 与 `MemoryHandler(Handler target, int size, Level pushLevel)`。
- **核心**：`publish(LogRecord)` 入循环缓冲，等级 ≥ pushLevel 时 `push()`；`push()` 把缓冲刷到 target 并清空；`flush()` 只 flush target 不 push；`close()` 关 target 并置自身 OFF；`get/setPushLevel`、`isLoggable`。

## 2. Java 用户如何使用

- `new MemoryHandler(target, 1000, Level.SEVERE)` 在错误级别时批量冲刷。
- `mh.publish(record);` 低等级先缓冲，`mh.push();` 手动冲刷。

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/logging/IsLoggableHandlerTest.java:52`：`new MemoryHandler(h, 1, Level.ALL)`。
- `test/jdk/java/util/logging/MemoryHandlerTest.java:103-104`：`new MemoryHandler()` + `publish(new LogRecord(Level.INFO, ...))`。
- `test/jdk/java/util/logging/HandlersConfigTest.java:118-122`：`new MemoryHandler()` 与 `new MemoryHandler(new SpecifiedHandler(), 100, Level.WARNING)`。

## 4. 测试用例设计 + 自动化实现

- **异常**：null target/pushLevel 抛 NPE；size≤0 抛 IAE；`setPushLevel(null)` 抛 NPE。
- **正常**：默认 pushLevel SEVERE、level ALL；低等级缓冲不 push、flush 不 push、push 级记录整体冲刷（顺序正确）、显式 `push()`、`close()` 关 target 并置 OFF。
- **健壮性**：内存 target Handler，无文件/网络/全局状态。
