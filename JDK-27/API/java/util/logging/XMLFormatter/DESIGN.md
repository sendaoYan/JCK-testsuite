# java.util.logging.XMLFormatter 测试方案

白盒结构断言在 `XMLFormatterTest.testWhiteBox()`，黑盒行为断言在 `XMLFormatterTest.testBlackBox()`。

## 1. API 功能

- **继承**：`extends Formatter`；无参构造器。
- **format(LogRecord)**：输出一条 `<record>` XML，含 `<date>/<millis>/<sequence>/<logger>/<level>/<class>/<method>/<thread>/<message>` 等元素，消息做 XML 转义。
- **getHead(Handler)**：输出 `<?xml ...?>`、`<!DOCTYPE ...>` 与 `<log>` 开标签。
- **getTail(Handler)**：输出 `</log>` 闭标签。

## 2. Java 用户如何使用

- 作为 FileHandler 的默认格式器，产出可归档 XML 日志。
- 直接格式化：`String xml = new XMLFormatter().format(record);`
- 文档组装：`getHead(h)` + 多条 format + `getTail(h)`。

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源。

- `src/java.logging/share/classes/java/util/logging/FileHandler.java:configure`：`new XMLFormatter()` 作为 FileHandler 默认格式器。
- `test/jdk/java/util/logging/SerializeLogRecordTest.java:54`：`XMLFormatter formatter = new XMLFormatter();` —— 官方测试。
- `test/jdk/java/util/logging/LoggingNIOChange.java:35`：`XMLFormatter f = new XMLFormatter();` —— 官方测试。

## 4. 测试用例设计 + 自动化实现

- **正常**：`format` 以 `<record>` 开头，含 `<level>INFO</level>`、`<message>hello</message>`、`<logger>`、`<millis>`、`<sequence>`。
- **组合**：`getHead` 含 `<?xml`/`<!DOCTYPE`/`<log>`，`getTail` 含 `</log>`。
- **边界**：消息含 `< & >` 时被转义为 `&lt; &amp; &gt;`；等级 WARNING 对应 `<level>WARNING</level>`。
- **健壮性**：固定 millis，不断言具体时间戳字符串。
