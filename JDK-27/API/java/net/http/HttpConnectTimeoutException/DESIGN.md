# java.net.http.HttpConnectTimeoutException 设计文档

## 1. API 功能

连接建立超时的专用异常，是 `HttpTimeoutException` 的子类。

- 构造器：`HttpConnectTimeoutException(String message)`（唯一构造器，无无参构造）。
- 继承链：`HttpConnectTimeoutException` → `HttpTimeoutException` → `IOException` → `Exception` → `Throwable`。
- 继承的查询/链方法：`getMessage()`、`getCause()`、`initCause(Throwable)`、`getStackTrace()`、`toString()`。

## 2. Java 用户如何使用

- 捕获型用法：用户在 `HttpClient.send/sendAsync` 上连接失败时 `catch (HttpConnectTimeoutException e)`，用 `e.getMessage()` 报告超时。
- 链式诊断：`e.initCause(rootCause)` 后通过 `e.getCause()` 取根因；`e.getStackTrace()` 定位调用点。
- 兜底：因为它同时是 `HttpTimeoutException` 与 `IOException`，可按超时类或 IO 类统一处理。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.net.http/share/classes/jdk/internal/net/http/`）：连接建立/重试路径中抛出该异常以表达「连接阶段超时」，与 `HttpTimeoutException`（整体请求超时）区分。
- 社区常见形态：用户侧通常在 `send`/`sendAsync` 的超时处理中按 `HttpConnectTimeoutException` 细分「连接超时」场景。

## 4. 测试用例设计 + 自动化实现

文件：`HttpConnectTimeoutExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 直接父类 `HttpTimeoutException` / 唯一 `String` 构造器。
- 黑盒：构造→message 保留→层级（`HttpTimeoutException`、`IOException`）→无 cause→堆栈非空→
  `initCause` 返回 this 且 cause 可读→二次 `initCause` 抛 `IllegalStateException`→`toString` 含 message。

## 6. 缺口分析（本批深化）

原 14 条断言缺少序列化与精确消息。本批追加至 32 条：

- 形状（probed）：仅一个构造器；仅 serialVersionUID 字段；svuid = 332；父类 svuid =
  981344271622632951。
- 行为：toString 精确格式（含/不含 message）；null message 合法；suppressed；
  父类 catch 捕获且 instanceof 区分；赋值给父类引用。
- 序列化（probed）：round-trip 保留 message 与类型层级。
