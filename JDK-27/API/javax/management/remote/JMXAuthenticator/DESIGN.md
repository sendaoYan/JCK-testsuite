# JMXAuthenticator 测试设计

## 1. API 功能
`javax.management.remote.JMXAuthenticator`（接口）为连接器服务器做客户端认证。

- 方法：`Subject authenticate(Object credentials)` —— 校验凭证并返回授权主体；失败时抛 `SecurityException`。

## 2. Java 用户如何使用
```java
JMXAuthenticator auth = new JMXAuthenticator() {
    public Subject authenticate(Object credentials) {
        // 校验 credentials，返回包含 JMXPrincipal 的 Subject
        return new Subject();
    }
};
```
典型组合链路：`RMIConnectorServer` 通过环境里的 `AUTHENTICATOR` 属性获取实现；客户端 `connect(env)` 携带凭证，服务器调用 `authenticate`。

## 3. 开源/源码实际用法调研
- `test/jdk/javax/management/remote/mandatory/passwordAuthenticator/SimpleStandard.java`、`test/jdk/javax/management/remote/mandatory/...`：自定义 `JMXAuthenticator` 校验用户名/密码并构造 `Subject`。
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIServerImpl.java`：`doNewClient` 使用环境里的 `JMXAuthenticator`。
来源均为 JDK 仓库（jdk25u-dev）源码与 `test/jdk`。

## 4. 测试用例设计
白盒：`isInterface()`、`authenticate(Object)` 返回 `Subject`。
黑盒轻行为：匿名实现证明 `authenticate` 可调用，接受 null 与非 null 凭证并返回同一 `Subject`。
