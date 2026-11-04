# KerberosKey 测试设计

## 1. API 功能
`javax.security.auth.kerberos.KerberosKey` 封装一个 Kerberos 主体的长期密钥，实现 `javax.crypto.SecretKey`。

- 构造器：`KerberosKey(KerberosPrincipal, byte[] keyBytes, int keyType, int versionNum)`（读 keytab 场景）；`KerberosKey(KerberosPrincipal, char[] password, String algorithm)`（密码派生，需触发 KDC/密码派生，测试规避）。
- 查询：`getPrincipal()`、`getVersionNumber()`、`getKeyType()`、`getAlgorithm()`、`getFormat()`（"RAW"）、`getEncoded()`（副本）。
- 生命周期：`destroy()`、`isDestroyed()`；`equals`/`hashCode`/`toString`。

## 2. Java 用户如何使用
JAAS 登录模块在 commit 阶段把主体长期密钥放进 Subject 私有凭证集：

```java
KerberosPrincipal p = new KerberosPrincipal("alice@EXAMPLE.COM");
KerberosKey k = new KerberosKey(p, keyBytes, 17, 3); // AES128, kvno 3
int version = k.getVersionNumber();
byte[] copy = k.getEncoded();
```

## 3. 开源代码实际用法调研
- JDK 源码自用（`~/git/jdk25u-dev/src/java.security.jgss/.../kerberos/KeyTab.java`）：`getKeys()` 内部 `new KerberosKey(principal, keys[i].getBytes(), keys[i].getEType(), kvno)`，正是 byte[] 构造器的真实调用。
- JDK 自带测试 `KerberosNullsAndDestroyTest.java`：`new KerberosKey(c, new byte[16], 17, 1)` 后 destroy，断言 6 个 getter 抛 `IllegalStateException`；`new KerberosKey(c, null, 17, 1)` 抛 NPE。
- JDK 自带测试 `KerberosHashEqualsTest.java`：比较 principal/keyBytes/keyType/version 对 equals 的影响，及 null principal 与销毁后的行为。

来源：JDK-27 对应实现与 `test/jdk`。

## 4. 测试用例设计 + 自动化实现
对应 `KerberosKeyTest.java` 黑盒部分：
1. byte[] 构造：principal/version/keyType/algorithm/format/getEncoded。
2. 构造器复制输入、getEncoded 返回副本。
3. equals/hashCode：逐字段差异（principal/version/keyType/material）验证不等。
4. null keyBytes 抛 NPE。
5. destroy 生命周期：isDestroyed、toString、hashCode==17、6 个 getter 抛 ISE。
规避：不使用 `(principal, char[], algorithm)` 构造器，避免密码派生/KDC。
