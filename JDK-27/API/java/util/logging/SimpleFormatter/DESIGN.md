# java.util.logging.SimpleFormatter 测试方案

白盒结构断言在 `SimpleFormatterTest.testWhiteBox()`，黑盒行为断言在 `SimpleFormatterTest.testBlackBox()`。

## 1. API 功能

- **继承**：`extends Formatter`；无参构造器。
- **format(LogRecord)**：按 `java.util.logging.SimpleFormatter.format` 系统属性（缺省用实现默认格式）输出一行摘要，包含时间、来源、logger、级别、消息、异常。
- **继承的 getHead/getTail**：默认返回 `""`。

## 2. Java 用户如何使用

- 配置到 Handler：`new StreamHandler(out, new SimpleFormatter())`。
- 直接格式化：`String line = new SimpleFormatter().format(record);`
- 自定义格式：`-Djava.util.logging.SimpleFormatter.format="%4$s: %5$s%n"`。

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/logging/IsLoggableHandlerTest.java:53`：`new StreamHandler(System.out, new SimpleFormatter())` —— StreamHandler 配 SimpleFormatter。
- `test/jdk/java/util/logging/SimpleFormatterFormat.java:38,168`：官方测试自定义 `SimpleFormatter.format` 属性。
- `src/java.logging/share/classes/java/util/logging/SimpleFormatter.java:159-185`：`format` 实现。

## 4. 测试用例设计 + 自动化实现

- **正常**：固定 millis/logger/message 后断言 `format` 输出含消息与 logger 名。
- **组合**：参数消息 `{0}/{1}` 经 format 被替换；thrown 被追加（含异常类名）。
- **边界**：`getHead/getTail` 默认 `""`。
- **健壮性**：固定 millis 避免墙钟，不断言时间戳具体值（跨时区一致）。
