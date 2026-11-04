# java.security.CodeSigner 黑盒行为测试设计

## 1. API 功能
- 构造：`CodeSigner(CertPath, Timestamp)`
- 查询：`getSignerCertPath()`、`getTimestamp()`、`equals/hashCode/toString`

## 2. Java 用户如何使用
JarFile 校验签名 jar 时对每个签名块构造 CodeSigner；用户从 `CodeSource.getCodeSigners()` 读取。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/CodeSigner.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/util/jar/JarFile/IgnoreUnrelatedSignatureFiles.java`、`/data/yansendao/git/jdk25u-dev/test/jdk/java/util/jar/JarInputStream/ScanSignedJar.java`（签名 jar → CodeSigner 全链路）

## 4. 测试用例设计 + 自动化实现
- 正常：固定 CertPath+Timestamp 构造，getter 返回同一实例，equals/hashCode 自反且 null-timestamp 相互判定一致
- 组合：CodeSigner 装入 CodeSource 后可读回；无/有 Timestamp 的 equals 区分
- 异常：null CertPath → NPE

断言入口：`CodeSignerTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
