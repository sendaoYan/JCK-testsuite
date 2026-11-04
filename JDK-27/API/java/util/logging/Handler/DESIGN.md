# java.util.logging.Handler 测试方案

白盒结构断言在 `HandlerTest.testWhiteBox()`，黑盒行为断言在 `HandlerTest.testBlackBox()`。

## 1. API 功能

- **抽象类**：protected 构造器；抽象方法 `publish/flush/close`。
- **等级/过滤/格式/编码/错误管理**：`setLevel/getLevel`（默认 ALL）、`setFilter/getFilter`（默认 null）、`setFormatter/getFormatter`（默认 null）、`setEncoding/getEncoding`（默认 null）、`setErrorManager/getErrorManager`（默认 ErrorManager）。
- **判定**：`isLoggable(LogRecord)`（null 记录返回 false）；`reportError(...)`（protected）。

## 2. Java 用户如何使用

- 自定义处理器：`new Handler(){ publish/flush/close 覆写 }`。
- 配置链：`h.setLevel(Level.WARNING); h.setFormatter(new SimpleFormatter()); h.setEncoding("UTF-8");`
- 判定：`if (h.isLoggable(record)) h.publish(record);`

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/logging/IsLoggableHandlerTest.java:27,72-78`：官方验证 `Handler.isLoggable(null)` 返回 false 与 setLevel 行为。
- `test/jdk/java/util/logging/HandlersConfigTest.java:288,313`：`SpecifiedHandler extends Handler`、`ConfiguredHandler extends Handler` —— 自定义 Handler 子类。
- `src/java.logging/share/classes/java/util/logging/Handler.java:86-90`：默认等级 ALL、默认 ErrorManager。

## 4. 测试用例设计 + 自动化实现

- **正常**：默认值（ALL/null/null/ErrorManager/null）；各 set/get 往返。
- **异常**：`setLevel(null)`、`setFormatter(null)`、`setErrorManager(null)` 抛 NPE；`setEncoding("no-such-charset")` 抛 UnsupportedEncodingException；`setFilter(null)` 合法。
- **组合**：`setLevel(WARNING)` 后 `isLoggable` 对 SEVERE/WARNING/INFO 的三态 + null 记录；`setFilter` + `publish` 记录捕获。
- **健壮性**：内存捕获 Handler，无全局状态。
