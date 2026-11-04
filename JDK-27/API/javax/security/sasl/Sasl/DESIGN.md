# javax.security.sasl.Sasl 测试设计

## 1. API 功能
SASL 静态工具类（构造 private），提供机制协商入口与协议常量：
- 工厂：`createSaslClient(String[] mechs, authzid, protocol, serverName, props, CallbackHandler)`、`createSaslServer(mech, protocol, serverName, props, CallbackHandler)`。
- 工厂枚举：`getSaslClientFactories()`、`getSaslServerFactories()`。
- 属性/策略常量：`QOP/STRENGTH/SERVER_AUTH/BOUND_SERVER_NAME/MAX_BUFFER/RAW_SEND_SIZE/REUSE/POLICY_NOPLAINTEXT/POLICY_NOACTIVE/POLICY_NODICTIONARY/POLICY_NOANONYMOUS/POLICY_FORWARD_SECRECY/POLICY_PASS_CREDENTIALS/CREDENTIALS`。

## 2. Java 用户如何使用
客户端/服务端双向协商：
```java
SaslClient client = Sasl.createSaslClient(new String[]{"PLAIN"}, null, "PLAIN", host, null, handler);
byte[] response = client.hasInitialResponse() ? client.evaluateChallenge(new byte[0]) : new byte[0];
SaslServer server = Sasl.createSaslServer("DIGEST-MD5", "imap", host, null, handler);
byte[] challenge = server.evaluateResponse(response);
```
典型链路：`createSaslClient → hasInitialResponse → evaluateChallenge → isComplete → getNegotiatedProperty(QOP) → dispose`。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/com/sun/security/sasl/Cram.java`（第 77–115 行）：完整客户端/服务端协商循环：
```java
SaslClient clnt = Sasl.createSaslClient(new String[]{MECH}, null, PROTOCOL, SERVER_FQDN, null, clntCbh);
SaslServer srv = Sasl.createSaslServer(MECH, PROTOCOL, SERVER_FQDN, null, srvCbh);
byte[] response = (clnt.hasInitialResponse()? clnt.evaluateChallenge(EMPTY) : EMPTY);
while (!clnt.isComplete() || !srv.isComplete()) {
    challenge = srv.evaluateResponse(response);
    if (challenge != null) response = clnt.evaluateChallenge(challenge);
}
```
- `digest/CheckNegotiatedQOPs.java` 等验证 DIGEST-MD5 的 QOP 协商。
- 常量值依据源码 `src/java.security.sasl/share/classes/javax/security/sasl/Sasl.java`（第 113–302 行）。

## 4. 测试用例设计 + 自动化实现
黑盒（`SaslTest.testBlackBox()`）：
- 14 个常量精确值；`getSaslClientFactories/getSaslServerFactories` 非空。
- `createSaslClient`：PLAIN + handler 非空并走完 `getMechanismName/hasInitialResponse/evaluateChallenge/isComplete/getNegotiatedProperty(QOP)==auth/dispose`；EXTERNAL 无 handler 非空；未知机制返回 null；PLAIN 无 handler 抛 `SaslException`。
- `createSaslServer`：PLAIN 返回 null（默认 provider 不提供）；DIGEST-MD5/CRAM-MD5 无 handler 抛 `SaslException`；未知机制返回 null。
白盒（`testWhiteBox()`）：public 类、声明 `createSaslClient/createSaslServer/getSaslClientFactories/getSaslServerFactories` 及 14 个常量字段。
