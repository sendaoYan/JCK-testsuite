# java.net.http.HttpTimeoutException 设计文档

## 1. API 功能

HTTP 请求整体超时的异常，直接继承 `IOException`。

- 构造器：`HttpTimeoutException(String message)`（唯一构造器）。
- 继承链：`HttpTimeoutException` → `IOException` → `Exception` → `Throwable`。
- 继承的查询/链方法：`getMessage()`、`getCause()`、`initCause(Throwable)`、`getStackTrace()`。

## 2. Java 用户如何使用

- 捕获型用法：`HttpRequest.timeout(Duration)` 超时后 `send/sendAsync` 抛出，用户 `catch (HttpTimeoutException e)`。
- 链式诊断：`initCause` + `getCause` 关联底层原因，`getStackTrace` 定位。
- 兜底：`IOException` 兜底可被通用 IO 处理逻辑捕获。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.net.http/share/classes/`）：请求超时被转换为 `HttpTimeoutException` 抛给用户；`HttpConnectTimeoutException` 是其连接超时细分。
- 社区常见形态：HTTP 客户端用户按 `HttpTimeoutException` 统一处理超时并决定是否重试。

## 4. 测试用例设计 + 自动化实现

文件：`HttpTimeoutExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 直接父类 `IOException` / 唯一 `String` 构造器。
- 黑盒：构造→message 保留→层级→无 cause→堆栈非空→`initCause` 链→二次 `initCause` 抛 `IllegalStateException`。

## 6. 缺口分析（本批深化）

原 12 条断言缺少序列化与精确消息。本批追加至 33 条：

- 形状（probed）：仅一个 String 构造器；仅 serialVersionUID 字段；svuid = 981344271622632951。
- 行为：toString 精确格式（含/不含 message）；null message 合法；suppressed 挂载；
  setStackTrace 清空；HttpConnectTimeoutException 为其子类。
- 序列化（probed）：round-trip 保留 message/cause/IOException 类型；identity equals。
