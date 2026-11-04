# com.sun.net.httpserver.Authenticator — 设计案

## 1. API 功能
- 抽象基类，用于实现 HTTP 认证机制（Basic/Digest 等），实例被服务器在每个请求进入时调用以校验认证信息。
- 核心方法：`public abstract Result authenticate(HttpExchange)`，返回认证结果。
- 嵌套结果类型（本组只测顶层类，但这些嵌套类型是 `authenticate` 的返回值，故作为组合断言对象使用）：
  - `Authenticator.Result`（抽象基类）
  - `Authenticator.Success(HttpPrincipal)` → `getPrincipal()`
  - `Authenticator.Retry(int responseCode)` → `getResponseCode()`
  - `Authenticator.Failure(int responseCode)` → `getResponseCode()`
- 受保护构造器：`protected Authenticator()`。

## 2. Java 用户如何使用
子类实现 `authenticate` 并返回上述三类结果之一：
```java
class Basic extends Authenticator {
    public Result authenticate(HttpExchange t) {
        if (ok) return new Authenticator.Success(new HttpPrincipal(u, realm));
        return new Authenticator.Failure(401);
    }
}
```
服务器侧通过 `HttpContext.setAuthenticator(a)` 挂载，`AuthFilter` 会根据返回的 Result 决定放行（Success）、重试（Retry）或拒绝（Failure）。

## 3. 开源代码实际用法调研
- JDK 源码自用：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/sun/net/httpserver/AuthFilter.java` 中 `Authenticator.Result r = authenticator.authenticate(t)`，随后 `instanceof Success/Retry/Failure` 分支处理。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/AuthenticatorTest.java` 构造 `new HttpPrincipal("test", "123")` 与 `Authenticator.Success`。
- `BasicAuthenticator`（本仓库同模块）是 `Authenticator` 的现成子类，返回 `Authenticator.Success/Failure/Retry`，见 `BasicAuthenticator.authenticate`。

## 4. 测试用例设计 + 自动化实现
- 白盒：抽象类、`authenticate` 方法、嵌套 `Result/Success/Retry/Failure` 的静态嵌套 + 继承关系（`AuthenticatorTest.testWhiteBox`）。
- 黑盒（`AuthenticatorTest.testBlackBox`）：
  - 匿名 `Authenticator` 返回 `Success`：断言类型为 Success、`getPrincipal()` 返回传入的 `HttpPrincipal`。
  - 匿名返回 `Retry(401)`：断言 `getResponseCode()==401`。
  - 匿名返回 `Failure(403)`：断言 `getResponseCode()==403`。
  - 三类结果都是 `Result` 子类，覆盖多步「构造 → authenticate → 分支判别 → 取值」链路。
