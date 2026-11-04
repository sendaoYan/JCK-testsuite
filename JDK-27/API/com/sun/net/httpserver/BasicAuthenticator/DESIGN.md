# com.sun.net.httpserver.BasicAuthenticator — 设计案

## 1. API 功能
- HTTP Basic 认证抽象实现，继承 `Authenticator`。
- 构造器：`BasicAuthenticator(String realm)`（用平台默认字符集解码凭据）、`BasicAuthenticator(String realm, Charset charset)`（JDK 14+）。
- 字段：`protected final String realm`。
- 方法：`getRealm()`；`abstract boolean checkCredentials(String username, String password)`（子类实现）；继承并实现 `authenticate(HttpExchange)`。
- 约束：realm 非空、必须是合法 quoted-string 内容；null realm 抛 NPE，空/非法 realm 抛 IAE。

## 2. Java 用户如何使用
```java
var auth = new BasicAuthenticator("realm", StandardCharsets.UTF_8) {
    public boolean checkCredentials(String u, String p) { return "user".equals(u) && "pass".equals(p); }
};
context.setAuthenticator(auth);
```
服务器收到请求后调用 `authenticate`：解析 `Authorization: Basic <base64(user:pass)>`，成功返回 `Authenticator.Success(new HttpPrincipal(user, realm))`，否则设置 `WWW-Authenticate` 头并返回 `Authenticator.Retry(401)`/`Failure(401)`。

## 3. 开源代码实际用法调研
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/com/sun/net/httpserver/BasicAuthenticator.java` 的 `authenticate` 用 `Base64.getDecoder()` 解码、`charset` 解码、`checkCredentials` 校验，并用 `setAuthHeader` 写 `WWW-Authenticate: Basic realm="..."[, charset="UTF-8"]`。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/BasicAuthenticatorRealm.java` 大量使用 `var server = HttpServer.create(new InetSocketAddress(LOOPBACK_ADDR, 0), 0); var handler = HttpHandlers.of(200, Headers.of(), "foo"); context.setAuthenticator(auth);`。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/BasicAuthToken.java` 在 handler 内用 `exchange.sendResponseHeaders(401, -1)` 与 `setAuthenticator(authenticator)`。

## 4. 测试用例设计 + 自动化实现
- 白盒：抽象类、继承 Authenticator、`getRealm/checkCredentials/authenticate`、两个构造器签名（`BasicAuthenticatorTest.testWhiteBox`）。
- 黑盒（`BasicAuthenticatorTest.testBlackBox`）：
  - 直接 `getRealm` + 匿名 `checkCredentials` 真/假。
  - 空 realm 抛 IAE、null realm 抛 NPE。
  - 真实内存 HTTP 回环：`HttpServer.create(ephemeral)` + `createContext("/")` + `setAuthenticator(auth)`；无凭据 → 401 + `WWW-Authenticate` 精确值；错误密码 → 401；正确 `user:pass` → 200 且 handler 里 `exchange.getPrincipal().getName()=="realm:user"`。
