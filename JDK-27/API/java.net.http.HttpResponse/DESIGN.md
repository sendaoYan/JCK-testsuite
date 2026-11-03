# java.net.http.HttpResponse 设计文档

## 1. API 功能

HTTP 响应的泛型接口 `HttpResponse<T>` 及其配套的 `BodyHandler`/`BodySubscriber`/`PushPromiseHandler`/`ResponseInfo`。

- 响应查询：`statusCode()`、`request()`、`headers()`、`body()`、`uri()`、`version()`、
  `previousResponse()`、`sslSession()`、默认方法 `connectionLabel()`。
- BodyHandler 工厂（`BodyHandlers`）：`ofString()`、`ofByteArray()`、`discarding()`、`replacing(U)` 等。
- BodySubscriber 工厂（`BodySubscribers`）：`ofString()`、`replacing(U)`、`discarding()` 等。
- PushPromiseHandler 工厂：`of(Function, ConcurrentMap)`。

## 2. Java 用户如何使用

- 组合链路：`client.send(request, BodyHandlers.ofString())` → `response.statusCode()` + `response.body()`。
- 流式协议：`BodyHandler.apply(ResponseInfo)` → `BodySubscriber` → `onSubscribe/onNext/onComplete` → `getBody()`。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.net.http/share/classes/java/net/http/HttpResponse.java` 与
  `jdk/internal/net/http/ResponseSubscribers.java`）：`replacing/discarding` 返回 `NullSubscriber`，
  其 `getBody()` 在 `onComplete` 后完成；`connectionLabel()` 为默认方法。

## 4. 测试用例设计 + 自动化实现

文件：`HttpResponseTest.java`（testWhiteBox + testBlackBox）。

- 白盒：interface + 方法返回类型 + `connectionLabel` 为 default + 四个嵌套类型。
- 黑盒（不真发请求）：匿名 `HttpResponse` 全方法 + 默认 `connectionLabel` →
  匿名 `ResponseInfo` + Flow 协议驱动 `BodyHandlers.replacing/discarding` 完成 →
  `BodyHandlers/BodySubscribers` 工厂非空 → `PushPromiseHandler.of`。
