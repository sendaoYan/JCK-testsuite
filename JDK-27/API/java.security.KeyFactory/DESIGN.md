# java.security.KeyFactory 黑盒行为测试设计

## 1. API 功能
- 工厂：`getInstance(String)`、`getInstance(String,String)`、`getInstance(String,Provider)`。
- 转换：`generatePublic(KeySpec)`、`generatePrivate(KeySpec)`、`getKeySpec(Key,Class)`、`translateKey(Key)`。
- 查询：`getAlgorithm()`、`getProvider()`。

## 2. Java 用户如何使用
`KeyFactory.getInstance("RSA")` → `generatePublic(new X509EncodedKeySpec(encoded))` / `generatePrivate(new PKCS8EncodedKeySpec(encoded))`；`getKeySpec(key, RSAPublicKeySpec.class)` 取算法参数。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/KeyFactory/GenerateRSAPrivateCrtKey.java`：generatePrivate 生成 CRT 私钥。
- `test/jdk/java/security/KeyFactory/KeyFactoryGetKeySpecForInvalidSpec.java`：getKeySpec 边界。
- 实现权威：`src/java.base/share/classes/java/security/KeyFactory.java`。

## 4. 测试用例设计 + 自动化实现
- RSA：X509/PKCS8 编码经 generatePublic/generatePrivate 往返后 getEncoded 与原始一致；getKeySpec 取 RSAPublicKeySpec 的 modulus/exponent 非空；translateKey 保持编码一致。
- EC：X509 编码往返。
- 白盒：concrete，含 getInstance/getProvider/getAlgorithm/generatePublic/generatePrivate/getKeySpec/translateKey。
