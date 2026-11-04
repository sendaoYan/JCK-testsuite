# javax.security.sasl.SaslServerFactory 测试设计

## 1. API 功能
SASL 服务端工厂接口，供 `Sasl` 按机制发现服务端实现：
- `String[] getMechanismNames(Map<String,?> props)`：返回该工厂支持的机制名。
- `SaslServer createSaslServer(String mech, protocol, serverName, props, CallbackHandler)`：为支持的机制创建服务端；不支持返回 null。

## 2. Java 用户如何使用
自定义机制 provider 实现此接口并通过 `Sasl.createSaslServer` 被自动发现；测试代码遍历 `Sasl.getSaslServerFactories()`：
```java
for (SaslServerFactory f : Sasl.getSaslServerFactories()) {
    String[] mechs = f.getMechanismNames(props);
    SaslServer s = f.createSaslServer("DIGEST-MD5", "imap", host, null, handler);
}
```
典型链路：`getMechanismNames → createSaslServer → getMechanismName → dispose`。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/com/sun/security/sasl/digest/`（如 `AuthRealms.java`、`CheckNegotiatedQOPs.java`）通过 `Sasl.createSaslServer(MECH, PROTOCOL, SERVER_FQDN, props, srvCbh)` 驱动 DIGEST-MD5 工厂。
- 接口方法签名依据 `javap`/源码 `src/java.security.sasl/share/classes/javax/security/sasl/SaslServerFactory.java`。默认 JDK 提供 DIGEST-MD5/CRAM-MD5/NTLM 等服务端机制（实测 `getSaslServerFactories()` 返回 4 个工厂）。

## 4. 测试用例设计 + 自动化实现
黑盒（`SaslServerFactoryTest.testBlackBox()`）：遍历 `Sasl.getSaslServerFactories()`，断言每个工厂 `getMechanismNames` 非 null、未知机制 `createSaslServer` 返回 null、至少一个工厂且机制并集非空；若存在 DIGEST-MD5 工厂，无 handler 的 `createSaslServer` 抛 `SaslException`。
白盒（`testWhiteBox()`）：public 接口、无父类、声明 `createSaslServer` 与 `getMechanismNames`。
