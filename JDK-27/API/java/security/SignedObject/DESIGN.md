# java.security.SignedObject 黑盒行为测试设计

## 1. API 功能
- 构造：`SignedObject(Serializable, PrivateKey, Signature)`。
- 查询：`getObject()`、`getSignature()`、`getAlgorithm()`、`verify(PublicKey, Signature)`。

## 2. Java 用户如何使用
把可序列化对象用私钥签名封装成 SignedObject，之后 `getObject()` 取回对象、`verify(pub, sig)` 校验签名。
```java
SignedObject so = new SignedObject(payload, privateKey, signingEngine);
boolean ok = so.verify(publicKey, verificationEngine);
```

## 3. 开源代码实际用法调研
- `test/jdk/java/security/SignedObject/Correctness.java`：签名正确性验证。
- `test/jdk/java/security/SignedObject/Copy.java`：序列化拷贝与 getSignature 防御性拷贝。
- 实现权威：`src/java.base/share/classes/java/security/SignedObject.java`。

## 4. 测试用例设计 + 自动化实现
- getObject 返回原始对象；getSignature 非空且为防御性拷贝；getAlgorithm 正确。
- 用正确公钥 verify 为 true，用另一把 RSA 公钥 verify 为 false。
- 白盒：final + Serializable，含 getObject/getSignature/getAlgorithm/verify。
