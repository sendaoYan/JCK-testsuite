# X500PrivateCredential 设计案

## 1. API 功能
`javax.security.auth.x500.X500PrivateCredential`（final，实现 `Destroyable`）关联 X.509 证书、对应私钥与
KeyStore 别名，便于在 `Subject` 中查找 X.500 主体的私有凭据。
- 构造器：`X500PrivateCredential(X509Certificate cert, PrivateKey key)`、
  `X500PrivateCredential(X509Certificate cert, PrivateKey key, String alias)`；任一参数 null 抛 `IllegalArgumentException`。
- 查询：`getCertificate()`、`getPrivateKey()`、`getAlias()`。
- 销毁：`destroy()`（置空三引用）、`isDestroyed()`（三者均 null 为 true）。

来源：`~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/x500/X500PrivateCredential.java`。

## 2. Java 用户如何使用
```java
X500PrivateCredential c = new X500PrivateCredential(cert, key, "alias");
c.getCertificate(); // cert
c.getPrivateKey();  // key
c.getAlias();       // "alias"
c.destroy();
c.isDestroyed();    // true（三引用均 null）
```
典型链路：`new(证书, 私钥[, alias]) → getCertificate/getPrivateKey/getAlias → destroy → isDestroyed`。

## 3. 开源代码实际用法调研
- JDK 源码：两个构造器对 cert/key（及 alias）做 null 检查，任一 null 抛 `IllegalArgumentException`；
  `destroy()` 置空三字段；`isDestroyed()` 判断三字段全 null。
- JDK 自带测试目录下未发现独立的 `X500PrivateCredential` 测试；本测试以 JDK 源码契约为准。
- 证书可传任意 `X509Certificate` 实例（本类仅存储/返回引用，不调用证书方法）；私钥用 `PrivateKey` 接口实现即可。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`X500PrivateCredentialTest.java` 的 `testBlackBox()`）：
- 构造异常：null 证书、null 私钥、null 别名分别抛 `IllegalArgumentException`。
- 双参构造：`getCertificate`/`getPrivateKey` 返回原引用、`getAlias` 为 null、初始 `isDestroyed` false。
- 三参构造：三个 getter 返回对应值、初始未销毁。
- 销毁：`destroy` 后 `isDestroyed` true 且三个 getter 均 null；`destroy` 幂等。
白盒补齐：`public final`、实现 `Destroyable`。
（测试用 `FakeCert extends X509Certificate` 与匿名 `PrivateKey` 作为确定性替身，仅满足抽象方法。）
