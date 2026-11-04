# com.sun.net.httpserver.HttpPrincipal — 设计案

## 1. API 功能
- 表示 HTTP Basic/Digest 认证的用户，实现 `java.security.Principal`。
- 构造器：`HttpPrincipal(String username, String realm)`；二者任一为 null 抛 NPE。
- 方法：`getUsername()`、`getRealm()`、`getName()`（`realm:username`）、`toString()`（同 getName）、`equals/hashCode`（按 username+realm）。

## 2. Java 用户如何使用
```java
HttpPrincipal p = new HttpPrincipal("alice", "realm");
String name = p.getName();      // "realm:alice"
```
认证成功后由 `Authenticator.Success(p)` 携带，handler 用 `exchange.getPrincipal()` 读取。

## 3. 开源代码实际用法调研
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/com/sun/net/httpserver/HttpPrincipal.java` 实现 `getName = String.format("%s:%s", realm, username)`、`hashCode = (username+realm).hashCode()`、`equals` 按 username+realm。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/AuthenticatorTest.java` 中 `new HttpPrincipal("test", "123")`。
- JDK 源码：`BasicAuthenticator.authenticate` 成功路径 `new Authenticator.Success(new HttpPrincipal(uname, realm))`。

## 4. 测试用例设计 + 自动化实现
- 白盒：具体类、实现 Principal、`(String,String)` 构造器（`HttpPrincipalTest.testWhiteBox`）。
- 黑盒（`HttpPrincipalTest.testBlackBox`）：
  - username/realm/name（`realm:user`）/toString==name。
  - equals/hashCode：相同、不同 username、不同 realm、非 HttpPrincipal、null。
  - 空字符串：name==`":"`、hashCode==`"".hashCode()`。
  - null 参数抛 NPE（username 与 realm 分别）。
