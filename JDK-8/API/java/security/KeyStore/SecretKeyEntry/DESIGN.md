# java.security.KeyStore.SecretKeyEntry 黑盒行为测试设计

## 1. API 功能
对称密钥条目：构造器 `SecretKeyEntry(SecretKey)`（JDK-8 无 attributes 版本）、`getSecretKey()`、继承的 `getAttributes()`。

## 2. 使用方式
`new KeyStore.SecretKeyEntry(key)` → `ks.setEntry(alias, entry, new PasswordProtection(pw))`；仅 JCEKS/PKCS12 可存（JKS 拒绝 SecretKey）。

## 3. 参考来源
- javadoc `java.security.KeyStore.SecretKeyEntry`。
- OpenJDK `test/jdk/java/security/KeyStore/SecretKeyEntry/`。
- JCK runtime-8b `api/java_security/KeyStore/`。

## 4. 测试用例设计 + 易错规范细节
- 构造 null → NullPointerException("invalid null input")；getSecretKey 身份往返；默认属性集为空；toString 含算法名（"Secret key entry with algorithm AES"）。
- identity 相等：同 key、值相等 key 的不同实例均不等（无 equals 覆写）。
- 格式矩阵：JKS setKeyEntry(SecretKey) → KeyStoreException("Cannot store non-PrivateKeys")；JCEKS/PKCS12 全闭环（setEntry→getEntry 类型往返、getKey 算法、encoded 字节往返、isKeyEntry true、证书链 null）；错口令 → UnrecoverableKeyException；deleteEntry 生命周期。
- 易错点：PKCS12 需口令非空条目保护；SecretKeySpec 造 key 最稳定（KeyGenerator 每次随机，断言只能比 encoded）。

断言入口：`SecretKeyEntryTest.java` 的 `testContractDeep()`（原有结构断言保留）。
