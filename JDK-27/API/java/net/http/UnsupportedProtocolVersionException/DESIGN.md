# java.net.http.UnsupportedProtocolVersionException 设计文档

## 1. API 功能

请求指定的协议版本不受支持时抛出的异常，final 类，继承 `IOException`。

- 构造器：`UnsupportedProtocolVersionException(String message)`（唯一构造器）。
- 继承链：`UnsupportedProtocolVersionException` → `IOException` → `Exception` → `Throwable`。
- 继承的查询/链方法：`getMessage()`、`getCause()`、`initCause(Throwable)`、`getStackTrace()`。

## 2. Java 用户如何使用

- 捕获型用法：`HttpRequest.version(...)` 指定不支持的版本后发送时 `catch (UnsupportedProtocolVersionException e)`。
- 链式诊断：`initCause` + `getCause` + `getStackTrace`。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.net.http/share/classes/`）：请求版本协商失败时抛出该异常，向用户表明「该版本不被支持」。
- 社区常见形态：HTTP 客户端按此异常回退到受支持版本或报错。

## 4. 测试用例设计 + 自动化实现

文件：`UnsupportedProtocolVersionExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / final / 父类 `IOException` / 唯一 `String` 构造器。
- 黑盒：构造→message 保留→层级→无 cause→堆栈非空→`initCause` 链→二次 `initCause` 抛 `IllegalStateException`。

## 6. 缺口分析（本批深化）

原 11 条断言缺少序列化与精确消息。本批追加至 30 条：

- 形状（probed）：仅一个构造器；仅 serialVersionUID 字段；svuid = 981344214212332893；
  与 HttpTimeoutException 是兄弟类（互不继承）。
- 行为：toString 精确格式（含/不含/空 message）；null message 合法；多个 suppressed 按序；
  空 message toString 以 ": " 结尾。
- 序列化（probed）：round-trip 保留 message 与 IOException 类型。
