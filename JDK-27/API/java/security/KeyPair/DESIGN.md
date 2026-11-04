# java.security.KeyPair 黑盒行为测试设计

## 1. API 功能
- 构造：`KeyPair(PublicKey, PrivateKey)`。
- 查询：`getPublic()`、`getPrivate()`。
- 实现 `Serializable` 与（JDK-27 预览）`BinaryEncodable`。

## 2. Java 用户如何使用
`KeyPair kp = keyPairGenerator.generateKeyPair(); PublicKey pub = kp.getPublic();` 或显式 `new KeyPair(pub, priv)` 封装已有密钥对。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/KeyPairGenerator/GenerateKeypair.java`：generateKeyPair 返回 KeyPair 后取公/私钥。
- 实现权威：`src/java.base/share/classes/java/security/KeyPair.java`。

## 4. 测试用例设计 + 自动化实现
- 生成 RSA 密钥对后，`new KeyPair(pub, priv)` 的 getPublic/getPrivate 与传入对象同一引用；算法名/编码非空。
- 白盒：final + Serializable + BinaryEncodable（字符串反射断言），含 getPublic/getPrivate。
