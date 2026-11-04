# javax.security.sasl.SaslServer 测试设计

## 1. API 功能
SASL 服务端接口，定义服务端侧协商契约（8 个方法）：
- 元信息：`getMechanismName()`、`isComplete()`。
- 协商：`byte[] evaluateResponse(byte[]) throws SaslException`；完成后 `getAuthorizationID()`。
- 保护层：`byte[] unwrap(byte[], int, int)`、`byte[] wrap(byte[], int, int)`。
- 查询/清理：`getNegotiatedProperty(String)`、`dispose()`。

## 2. Java 用户如何使用
通过 `Sasl.createSaslServer` 获取实例后响应客户端：
```java
SaslServer server = Sasl.createSaslServer(mech, protocol, serverName, props, handler);
byte[] challenge = server.evaluateResponse(clientResponse);
if (server.isComplete()) { String authz = server.getAuthorizationID(); }
server.dispose();
```
典型链路：`getMechanismName → evaluateResponse → isComplete → getAuthorizationID → getNegotiatedProperty → dispose`。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/com/sun/security/sasl/Cram.java`（第 104–112 行）：`srv.evaluateResponse(response)` 与 `srv.isComplete()`，成功后打印 `srv.getAuthorizationID()`。
- 接口方法签名依据 `javap`/源码 `src/java.security.sasl/share/classes/javax/security/sasl/SaslServer.java`。

## 4. 测试用例设计 + 自动化实现
黑盒（`SaslServerTest.testBlackBox()`）：最小确定性匿名实现，覆盖 `getMechanismName`、`isComplete` 在 `evaluateResponse` 前后翻转、`getAuthorizationID` 完成前 null/完成后取值、`unwrap/wrap` 按 `(off,len)` 拷贝区间、`getNegotiatedProperty` 已知/未知、`dispose` 可调用。
白盒（`testWhiteBox()`）：public 接口、无父类、声明 8 个方法。
