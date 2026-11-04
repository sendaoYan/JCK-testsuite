# javax.security.cert.X509Certificate 测试设计

## 1. API 功能
遗留（`@Deprecated(forRemoval=true)`）X.509 v1 证书抽象类，继承 `javax.security.cert.Certificate`：
- 工厂：`static X509Certificate getInstance(byte[])`、`getInstance(InputStream)`（通过安全属性 `cert.provider.x509v1` 反射加载实现类，失败抛 `CertificateException`）。
- 有效期：`checkValidity()` / `checkValidity(Date)`。
- 属性查询（均为 abstract）：`getVersion/getSerialNumber/getIssuerDN/getSubjectDN/getNotBefore/getNotAfter/getSigAlgName/getSigAlgOID/getSigAlgParams`。
- 继承自 `Certificate`：`getEncoded/getPublicKey/verify/verify(String)/toString/equals/hashCode`。

## 2. Java 用户如何使用
```java
X509Certificate cert = X509Certificate.getInstance(inStream); // 或 getInstance(byte[])
cert.checkValidity();
int ver = cert.getVersion();
BigInteger serial = cert.getSerialNumber();
Principal issuer = cert.getIssuerDN();
Principal subject = cert.getSubjectDN();
Date nb = cert.getNotBefore(), na = cert.getNotAfter();
String alg = cert.getSigAlgName(), oid = cert.getSigAlgOID();
```
典型链路：`getInstance → checkValidity → getSubjectDN/getSerialNumber/getNotBefore/getNotAfter → getPublicKey/verify`。

## 3. 开源代码实际用法调研
- JDK 源码 javadoc 示例（`src/java.base/share/classes/javax/security/cert/X509Certificate.java` 第 83–89 行）：`X509Certificate cert = X509Certificate.getInstance(inStream);` 与 `getInstance(certData);`。
- `getInst` 源码（第 195–235 行）：通过 `Class.forName("com.sun.security.cert.internal.x509.X509V1CertImpl")` 反射构造，任何加载/解析异常统一包装为 `CertificateException`。实测 JDK-27 Temurin 构建中该内部类不可达，因此对任意输入（含垃圾字节）都确定性地抛 `CertificateException`（消息含 "Could not find class"）。
- 注意：`javax.security.cert.X509Certificate` 并**不**实现 `X509Extension`（`javap` 显示声明为 `extends Certificate` 且无 implements），子类只需实现自身与 `Certificate` 的 abstract 方法。

## 4. 测试用例设计 + 自动化实现
黑盒（`X509CertificateTest.testBlackBox()`）：
- 匿名具体子类固定 11 个 X509 属性方法 + `getEncoded/getPublicKey/verify/toString`，断言各 getter 返回值、`checkValidity()`/`checkValidity(Date)` 可调用、`getIssuerDN/getSubjectDN` 的 `Principal.getName`。
- `getInstance(byte[])` 与 `getInstance(InputStream)` 对垃圾字节断言抛 `CertificateException`。
白盒（`testWhiteBox()`）：public、abstract class、父类为 `Certificate`、有 public 构造、声明 `checkValidity/getVersion/getSerialNumber/getIssuerDN/getSubjectDN/getNotBefore/getNotAfter/getSigAlgName/getSigAlgOID/getSigAlgParams/getInstance`。
