# java.util.logging.ConsoleHandler 测试方案

白盒结构断言在 `ConsoleHandlerTest.testWhiteBox()`，黑盒行为断言在 `ConsoleHandlerTest.testBlackBox()`。

## 1. API 功能

- **继承**：`extends StreamHandler`；无参构造器。
- **行为**：把日志写到 `System.err`；默认等级 INFO、默认格式器 SimpleFormatter；`publish` 后立即 `flush`；`close` 只 flush 不关闭 System.err。

## 2. Java 用户如何使用

- 作为根 logger 的默认控制台输出：`Logger.getLogger("").addHandler(new ConsoleHandler());`
- 用户侧一般只读配置，不直接 publish（会打到真实 stderr）。

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/logging/DefaultConfigTest.java:91`：`if (root.getHandlers()[0].getClass() != ConsoleHandler.class)` —— 默认根 handler 类型断言。
- `test/jdk/java/util/logging/HandlersConfigTest.java:138`：`new ConsoleHandler()` —— 官方测试。
- `src/java.logging/share/classes/java/util/logging/ConsoleHandler.java:68-72`：构造器 `super(Level.INFO, new SimpleFormatter(), null)` + `setOutputStream(System.err)`。

## 4. 测试用例设计 + 自动化实现

- **正常**：构造（不写 stderr），断言默认 level INFO、formatter SimpleFormatter、encoding null。
- **组合**：`setLevel(WARNING)` 往返；`isLoggable` 对 INFO/SEVERE/FINE 的三态（不触发输出）。
- **边界**：`close()` 只 flush、不抛异常。
- **健壮性**：全程不 publish（避免打真实控制台），纯 headless。
