# java.net.http.StreamLimitException 设计文档

## 1. API 功能

HTTP/2 并发流数量超过限制时抛出的异常，final 类，继承 `IOException`。

- 构造器：`StreamLimitException(HttpClient.Version version, String message)`。
- 查询：`version()` 返回关联的 HTTP 版本（`HTTP_1_1` / `HTTP_2` / `HTTP_3`）。
- 继承的查询/链方法：`getMessage()`、`getCause()`、`initCause(Throwable)`、`getStackTrace()`。

## 2. Java 用户如何使用

- 捕获型用法：HTTP/2 流上限被触发时 `catch (StreamLimitException e)`，用 `e.version()` 判断协议版本、`e.getMessage()` 报告原因。
- 链式诊断：`initCause` + `getCause`；`getStackTrace` 定位。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.net.http/share/classes/`）：HTTP/2 流管理在达到并发流上限时抛出 `StreamLimitException`，携带触发时的协议版本。
- 社区常见形态：HTTP/2 客户端按此异常调整并发流窗口或重试。

## 4. 测试用例设计 + 自动化实现

文件：`StreamLimitExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / final / 父类 `IOException` / `(Version,String)` 构造器 / `version()`。
- 黑盒：构造→message 保留→`version()` 读回（HTTP_2 与 HTTP_1_1）→`IOException` 层级→无 cause→
  堆栈非空→`initCause` 链→二次 `initCause` 抛 `IllegalStateException`。

## 6. 缺口分析（本批深化）

原 13 条断言缺少序列化与 null 边界。本批追加至 33 条：

- 形状（probed）：仅一个构造器；声明非 transient `version` 字段 + serialVersionUID；
  svuid = 2614981180406031159；version() 返回 HttpClient.Version。
- 行为（probed）：null version → NPE；null message 合法；toString 精确格式；suppressed；
  三种 Version（含 HTTP_3）均 round-trip。
- 序列化（probed）：version 非 transient，round-trip 后保留 HTTP_3；message 保留。
