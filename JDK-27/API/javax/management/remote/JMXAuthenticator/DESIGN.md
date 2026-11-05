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


---

## 深化（2026-11，批 m）：JDK-27 侧补充说明
本目录 `JMXAuthenticatorTest.java` 在既有白盒+黑盒之上追加 `testDeepened()`，与 JDK-8 侧同源（差异仅下列各点）。

### 新增覆盖维度
- 接口形态 + 用户实现三分支（好/坏/null 凭据）+ RMI 真闭环（id 携带 principal、异常透传）。

### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 本目录版本差异
### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
