# EncryptionKey 测试设计

## 1. API 功能
`javax.security.auth.kerberos.EncryptionKey` 封装 Kerberos 加密密钥（RFC 4120 4.2.9），实现 `javax.crypto.SecretKey`（JDK 9+）。

- 构造器：`EncryptionKey(byte[] keyBytes, int keyType)`，复制 keyBytes。
- 查询：`getKeyType()`、`getAlgorithm()`（IANA etype 名，含 none/unknown/private）、`getFormat()`（恒为 "RAW"）、`getEncoded()`（返回副本）。
- 生命周期：`destroy()`、`isDestroyed()`。
- 对象方法：`toString()`、`equals`/`hashCode`（keyType + key material；销毁后仅等于自身）。

## 2. Java 用户如何使用
用户从 KDC 应答或 keytab 拿到 key bytes 后封装密钥，用于 GSS 上下文或登录模块：

```java
EncryptionKey key = new EncryptionKey(rawBytes, 18); // AES256
int type = key.getKeyType();
byte[] copy = key.getEncoded();
key.destroy();
```

## 3. 开源代码实际用法调研
- JDK 源码自用（`~/git/jdk25u-dev/src/java.security.jgss/.../kerberos/KerberosTicket.java`）：`getSessionKey()` 返回 `new EncryptionKey(sessionKey.getEncoded(), sessionKey.getKeyType())`。
- JDK 自带测试 `KerberosNullsAndDestroyTest.java`：`new EncryptionKey(new byte[16], 17)` 后 destroy，断言 `getAlgorithm/getEncoded/getFormat/getKeyType` 均抛 `IllegalStateException`。
- JDK 自带测试 `KerberosHashEqualsTest.java`：验证 `EncryptionKey` 的 equals/hashCode 与销毁语义。

来源：JDK-27 对应实现与 `test/jdk`。

## 4. 测试用例设计 + 自动化实现
对应 `EncryptionKeyTest.java` 黑盒部分：
1. etype 18/17 的 keyType 与 IANA 算法名（`aes256-cts-hmac-sha1-96` / `aes128-cts-hmac-sha1-96`），format "RAW"。
2. 算法边界：etype 0→"none"、正数未知→"unknown"、负数→"private"。
3. 构造器复制输入、getEncoded 返回副本（多步：修改入参与返回值后原对象不变）。
4. equals/hashCode：同 type+material 相等，异 type/异 material 不等。
5. null keyBytes 抛 NPE。
6. destroy 生命周期：isDestroyed、toString、hashCode==17、各 getter 抛 ISE、两个销毁对象互不相等。
