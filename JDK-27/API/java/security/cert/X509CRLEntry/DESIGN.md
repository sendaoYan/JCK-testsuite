# java.security.cert.X509CRLEntry 黑盒行为测试设计

## 1. API 功能
CRL 条目：serial/revocationDate/hasExtensions/getRevocationReason/toString/getEncoded/getCertificateIssuer。

## 2. Java 用户如何使用
逐条吊销信息。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/X509CRL/ 条目用例。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：fixture 条目 serialNumber==0、revocationDate 固定、getCertificateIssuer 为 null（v1 CRL 无 issuer 别名）、hasExtensions==false、getRevocationReason（无 reason code 扩展时返回 null，实测）、getEncoded 非空。

断言入口：`X509CRLEntryTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
