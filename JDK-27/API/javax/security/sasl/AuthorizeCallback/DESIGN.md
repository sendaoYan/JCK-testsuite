# javax.security.sasl.AuthorizeCallback 测试设计

## 1. API 功能
SASL 授权回调，实现 `javax.security.auth.callback.Callback` 与 `java.io.Serializable`，用于在认证后询问"认证身份是否被授权为某授权身份"：
- 构造：`AuthorizeCallback(String authnID, String authzID)`。
- 查询：`getAuthenticationID()`、`getAuthorizationID()`、`getAuthorizedID()`、`isAuthorized()`。
- 设置：`setAuthorized(boolean)`、`setAuthorizedID(String)`。

## 2. Java 用户如何使用
SASL 服务端 `CallbackHandler.handle` 中处理：
```java
if (cb instanceof AuthorizeCallback) {
    AuthorizeCallback acb = (AuthorizeCallback) cb;
    if (acb.getAuthenticationID().equals(acb.getAuthorizationID()))
        acb.setAuthorized(true);
    acb.setAuthorizedID("canonical-id");
}
```
典型链路：`new AuthorizeCallback(authn, authz) → getAuthenticationID/getAuthorizationID → setAuthorized(true) → setAuthorizedID → isAuthorized/getAuthorizedID`。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/com/sun/security/sasl/digest/CheckNegotiatedQOPs.java`（第 145–152 行）：
```java
} else if (callbacks[i] instanceof AuthorizeCallback) {
    AuthorizeCallback cb = (AuthorizeCallback)callbacks[i];
    cb.setAuthorized(true);
}
```
- 类定义依据源码 `src/java.security.sasl/share/classes/javax/security/sasl/AuthorizeCallback.java`（字段 `authenticationID/authorizationID/authorizedID/authorized`，`authorized` 初始 false）。

## 4. 测试用例设计 + 自动化实现
黑盒（`AuthorizeCallbackTest.testBlackBox()`）：构造取 authn/authz ID、初始 `isAuthorized()==false`、`setAuthorized(true)` 后 `isAuthorized`、`setAuthorizedID` 后 `getAuthorizedID`、再次 `setAuthorized(false)` 复位、null ID 处理、`setAuthorizedID(null)`。
白盒（`testWhiteBox()`）：public、具体类、直接实现 `Callback` 与 `Serializable`、拥有 `(String,String)` 公共构造。
