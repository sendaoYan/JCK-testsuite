# java.util.logging.StreamHandler 测试方案

白盒结构断言在 `StreamHandlerTest.testWhiteBox()`，黑盒行为断言在 `StreamHandlerTest.testBlackBox()`。

## 1. API 功能

- **继承**：`extends Handler`；构造器 `StreamHandler()`（默认 System.err）与 `StreamHandler(OutputStream, Formatter)`。
- **核心**：`publish(LogRecord)` 写格式器头 + 消息，`flush()`、`close()`（写尾并关流）。
- **输出流/编码**：`setOutputStream(OutputStream)`（protected）、`setEncoding(String)`、`isLoggable(LogRecord)`（无 writer 或 null 记录返回 false）。

## 2. Java 用户如何使用

- 写内存/文件流：`new StreamHandler(new ByteArrayOutputStream(), new SimpleFormatter())`。
- 设编码：`sh.setEncoding("UTF-8"); sh.publish(record); sh.flush();`
- 作为 ConsoleHandler/FileHandler/SocketHandler 的基类。

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/logging/IsLoggableHandlerTest.java:53`：`new StreamHandler(System.out, new SimpleFormatter())`。
- `test/jdk/java/util/logging/HandlersConfigTest.java:128,132,174`：`new StreamHandler()`、`new StreamHandler(System.out, new SpecifiedFormatter())`。
- `src/java.logging/share/classes/java/util/logging/StreamHandler.java:194-238`：`publish` 写头+消息实现。

## 4. 测试用例设计 + 自动化实现

- **正常**：自定义 Formatter 暴露 getHead/getTail，断言最终输出精确等于 `HEAD\n[m1]\nTAIL\n`（头+消息+尾的完整生命周期）。
- **组合**：`setEncoding("UTF-8")` 后按 UTF-8 解码；`setLevel(WARNING)` 过滤低等级记录。
- **异常**：`StreamHandler(out, null)` 抛 NPE；`isLoggable` 对 FINE/INFO 与 writer 存在性。
- **健壮性**：内存 ByteArrayOutputStream，无文件/网络/全局状态。
