# javax.security.sasl.SaslClientFactory 测试设计

## 1. API 功能
SASL 客户端工厂接口，供 `Sasl` 按机制发现客户端实现：
- `String[] getMechanismNames(Map<String,?> props)`：返回该工厂支持的机制名。
- `SaslClient createSaslClient(String[] mechs, authzid, protocol, serverName, props, CallbackHandler)`：为支持的机制创建客户端；不支持返回 null。

## 2. Java 用户如何使用
自定义机制 provider 实现此接口并通过 `Sasl.createSaslClient` 被自动发现；测试代码遍历 `Sasl.getSaslClientFactories()`：
```java
for (SaslClientFactory f : Sasl.getSaslClientFactories()) {
    String[] mechs = f.getMechanismNames(props);
    SaslClient c = f.createSaslClient(new String[]{"PLAIN"}, null, "PLAIN", host, null, handler);
}
```
典型链路：`getMechanismNames → createSaslClient → getMechanismName → dispose`。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/com/sun/security/sasl/digest/CheckNegotiatedQOPs.java`、`Cram.java` 等通过 `Sasl.createSaslClient` 间接驱动各工厂实现。
- 接口方法签名依据 `javap`/源码 `src/java.security.sasl/share/classes/javax/security/sasl/SaslClientFactory.java`。默认 JDK 提供的客户端机制实测含 PLAIN/EXTERNAL/DIGEST-MD5/NTLM/CRAM-MD5 等（`getSaslClientFactories()` 返回 6 个工厂）。

## 4. 测试用例设计 + 自动化实现
黑盒（`SaslClientFactoryTest.testBlackBox()`）：遍历 `Sasl.getSaslClientFactories()`，断言每个工厂 `getMechanismNames` 非 null、未知机制 `createSaslClient` 返回 null、至少一个工厂且并集中含 PLAIN；找到 PLAIN 工厂后调用 `createSaslClient` 得到非 null 客户端并校验 `getMechanismName=="PLAIN"` 后 `dispose`。
白盒（`testWhiteBox()`）：public 接口、无父类、声明 `createSaslClient` 与 `getMechanismNames`。
