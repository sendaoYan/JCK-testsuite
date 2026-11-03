# javax.security.sasl.SaslClient 测试设计

## 1. API 功能
SASL 客户端接口，定义客户端侧协商契约（8 个方法）：
- 元信息：`getMechanismName()`、`hasInitialResponse()`、`isComplete()`。
- 协商：`byte[] evaluateChallenge(byte[]) throws SaslException`。
- 保护层：`byte[] unwrap(byte[], int, int)`、`byte[] wrap(byte[], int, int)`。
- 查询/清理：`getNegotiatedProperty(String)`、`dispose()`。

## 2. Java 用户如何使用
通过 `Sasl.createSaslClient` 获取实例后协商：
```java
SaslClient client = Sasl.createSaslClient(mechs, authzid, protocol, server, props, handler);
byte[] response = client.hasInitialResponse() ? client.evaluateChallenge(new byte[0]) : null;
while (!client.isComplete()) response = client.evaluateChallenge(serverChallenge);
Object qop = client.getNegotiatedProperty(Sasl.QOP);
client.dispose();
```
典型链路：`getMechanismName → hasInitialResponse → evaluateChallenge → isComplete → getNegotiatedProperty → dispose`。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/com/sun/security/sasl/Cram.java`（第 100–112 行）：`clnt.hasInitialResponse()? clnt.evaluateChallenge(EMPTY) : EMPTY`，并在循环中 `clnt.evaluateChallenge(challenge)`、`clnt.isComplete()`。
- 接口方法签名依据 `javap`/源码 `src/java.security.sasl/share/classes/javax/security/sasl/SaslClient.java`。

## 4. 测试用例设计 + 自动化实现
黑盒（`SaslClientTest.testBlackBox()`）：用 `Sasl.createSaslClient("PLAIN", handler)` 取真实实现，断言 `getMechanismName/hasInitialResponse/isComplete`；`wrap/unwrap` 完成前抛 `IllegalStateException`、完成后（PLAIN 无完整/私密层）抛 `SaslException`；`evaluateChallenge` 返回非空初始响应并置 `isComplete`；`getNegotiatedProperty(QOP)==auth`、未知属性 null；`dispose` 可调用。
白盒（`testWhiteBox()`）：public 接口、无父类、声明 8 个方法。
