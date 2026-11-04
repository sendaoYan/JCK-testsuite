# java.util.logging.Formatter 测试方案

白盒结构断言在 `FormatterTest.testWhiteBox()`，黑盒行为断言在 `FormatterTest.testBlackBox()`。

## 1. API 功能

- **抽象类**：`abstract String format(LogRecord record)`；protected 构造器。
- **模板方法**：`getHead(Handler)`、`getTail(Handler)`（默认返回 `""`）。
- **消息格式化**：`formatMessage(LogRecord)` —— 无参数返回原消息；含 `{0}/{1}` 时用 `MessageFormat` 替换；若设置了 ResourceBundle 则以消息为 key 先查 bundle。

## 2. Java 用户如何使用

- 自定义格式：`new Formatter(){ public String format(LogRecord r){ return r.getMessage(); } }`。
- 输出头尾：子类覆写 `getHead/getTail` 输出文档头尾（如 XML/CSV）。
- 参数化消息：`f.formatMessage(record)` 得到 `{0}/{1}` 替换后的字符串。

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源。

- `src/java.logging/share/classes/java/util/logging/SimpleFormatter.java:171`：`String message = formatMessage(record);` —— 子类复用 formatMessage。
- `src/java.logging/share/classes/java/util/logging/XMLFormatter.java`（format 内）：`formatMessage(record)` 后 escape 成 XML —— 同上。
- `src/java.logging/share/classes/java/util/logging/Formatter.java:113-160`：`formatMessage` 实现（bundle + MessageFormat）。

## 4. 测试用例设计 + 自动化实现

- **正常**：匿名子类 `format` 精确断言；`getHead/getTail` 默认 `""`。
- **组合/边界**：`formatMessage` 无参原样、`{0}/{1}` 精确替换、ResourceBundle key 解析 + 替换、null 消息返回 null。
- **健壮性**：固定消息与参数，无墙钟/随机/全局状态。
