# java.security.Certificate 黑盒行为测试设计

## 1. API 功能
- 方法：`getEncoded()`、`verify(PublicKey)`、`verify(PublicKey, String)`、`toString()`、`getPublicKey()`

## 2. Java 用户如何使用
JDK 1.1 遗留；X.509 证书用 `java.security.cert.Certificate`。此接口保留互操作。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/Certificate.java`（deprecated for removal）
- 对比 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/cert/X509Certificate.java`

## 4. 测试用例设计 + 自动化实现
- 白盒：interface + deprecated + 5 个方法签名
- 黑盒：自实现接口（固定返回值），组合断言 getEncoded/verify/toString/getPublicKey 的透传契约；verify(null) 抛自实现异常原样透传

断言入口：`CertificateTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

## 5. 追加契约维度（深化批）
- 结构：恰 7 声明方法、无 default/static、无父接口、非 Serializable、@Deprecated。
- encode/decode 的 throws 子句精确为 {KeyException, IOException}；toString(boolean) 布尔参数返回 String。
- 异常中继：实现抛出的 IOException/KeyException 原样（消息逐字）传播。
- equals 无值语义：同内容不同实现实例互不相等。
