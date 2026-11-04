# java.security.PrivateKey 黑盒行为测试设计

## 1. API 功能
- 接口 `PrivateKey extends AsymmetricKey, javax.security.auth.Destroyable`，含 `serialVersionUID`。
- 继承 Key 的 `getAlgorithm/getFormat/getEncoded`；继承 AsymmetricKey 的 `getParams()`；继承 Destroyable 的 `destroy()/isDestroyed()`。

## 2. Java 用户如何使用
从 `generateKeyPair().getPrivate()` 取得；`getEncoded()` 为 PKCS#8 编码；`getAlgorithm()`/`getFormat()` 用于持久化或重建。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/KeyFactory/GenerateRSAPrivateCrtKey.java`：私钥 getEncoded 后经 PKCS8EncodedKeySpec 重建。
- 实现权威：`src/java.base/share/classes/java/security/PrivateKey.java`。

## 4. 测试用例设计 + 自动化实现
- 真实 RSA 私钥：instanceof PrivateKey/AsymmetricKey/Key/Destroyable；getAlgorithm "RSA"、getFormat "PKCS#8"、getEncoded 非空；getParams 默认 null。
- 白盒：接口，父接口 AsymmetricKey 与 Destroyable（字符串反射断言）。
