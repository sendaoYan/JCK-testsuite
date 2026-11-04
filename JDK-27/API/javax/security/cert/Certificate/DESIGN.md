# javax.security.cert.Certificate 测试设计

## 1. API 功能
遗留（JDK9 起 `@Deprecated(forRemoval=true)`）的 X.509 证书抽象基类，定义证书的通用能力：
- 构造：无参 protected/public 构造，仅供子类调用。
- 查询：`abstract byte[] getEncoded()`（DER 编码）、`abstract String toString()`、`abstract PublicKey getPublicKey()`。
- 校验：`abstract void verify(PublicKey)` 与 `verify(PublicKey, String sigProvider)`。
- 通用语义：`boolean equals(Object)` 与 `int hashCode()` 依据 `getEncoded()` 字节内容比较（源码 `src/java.base/share/classes/javax/security/cert/Certificate.java` 中 `Arrays.equals`/`Arrays.hashCode`），`getEncoded()` 抛异常时 equals 返回 false、hashCode 返回 0。

## 2. Java 用户如何使用
用户不直接 new（抽象类），而是继承实现 `getEncoded`/`getPublicKey`/`verify`/`toString`，或使用 `javax.security.cert.X509Certificate.getInstance(...)` 工厂拿到具体实现：
```java
Certificate c = X509Certificate.getInstance(certBytes);
byte[] der = c.getEncoded();
PublicKey key = c.getPublicKey();
c.verify(key);
boolean same = c.equals(otherCert);   // 依据编码内容
int h = c.hashCode();
```
典型组合链路：`getInstance → getEncoded → equals/hashCode → getPublicKey → verify`。

## 3. 开源代码实际用法调研
该遗留类在 JDK 自身 `test/jdk` 中已无直接用例（`grep -rln "javax.security.cert" test/jdk` 返回空），说明已被 `java.security.cert` 取代。权威依据取自 JDK 源码：
- 源码 `/data/yansendao/git/jdk25u-dev/src/java.base/share/classes/javax/security/cert/Certificate.java` 的 `equals`/`hashCode` 实现（`Arrays.equals(this.getEncoded(), other.getEncoded())`、`Arrays.hashCode(this.getEncoded())`）。
- `X509Certificate.java` javadoc 示例（第 83–89 行）：`X509Certificate cert = X509Certificate.getInstance(inStream);` / `X509Certificate cert = X509Certificate.getInstance(certData);`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`CertificateTest.java` 的 `testBlackBox()`）：
- 用匿名子类固定 `getEncoded()` 返回内容，覆盖 `getEncoded`/`getPublicKey`/`toString` 的取值。
- equals 组合：同一实例、相同编码、不同编码、null、非 Certificate 对象；hashCode 相等性与 `Arrays.hashCode` 一致性。
- verify 两个重载可调用（继承实现为 no-op，证明抽象方法可被实现并调用）。
白盒部分（`testWhiteBox()`）保留并补齐：public、abstract class、有 public 构造、声明 `getEncoded/verify/toString/getPublicKey/equals/hashCode`。
