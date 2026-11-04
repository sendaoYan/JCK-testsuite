# java.security.cert.CertificateRevokedException 黑盒行为测试设计

## 1. API 功能
JDK 专有：证书被吊销的受检异常，携带 revocationDate、reason、authority、extensions（Map<String,Extension>）。

## 2. Java 用户如何使用
用户从 CRL/OCSP 实现处获得；getExtensions 暴露吊销时的扩展。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertificateRevokedException/Basic.java（真实用例，含 getX/getReason/getAuthority/getExtensions）。

## 4. 测试用例设计 + 自动化实现
黑盒：固定 Date/CRLReason.UNSPECIFIED/X500Principal CN=jck/一个 1.2.3.4.5 Extension 构造；getRevocationDate 返回克隆（改副本不影响）、getReason、getAuthority、getExtensions 含自定义 Extension 且 toString 含 message；InvalidAuthority 类引用。

断言入口：`CertificateRevokedExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
