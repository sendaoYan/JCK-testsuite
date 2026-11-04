# java.security.cert.X509Extension 黑盒行为测试设计

## 1. API 功能
扩展接口：hasUnsupportedCriticalExtension/getCriticalExtensionOIDs/getNonCriticalExtensionOIDs/getExtensionValue。

## 2. Java 用户如何使用
证书与 CRL 共同的扩展读取面。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/X509Certificate/ 扩展用例。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：证书与 CRL 都实现该接口；证书 critical={2.5.29.19}、non-critical 含 2.5.29.14、每个 OID 的 getExtensionValue 为非空 OCTET 包装 DER、未知 OID 返回 null、hasUnsupportedCriticalExtension==false；无扩展的 v1 CRL 两个 OID 集合均为 null。

断言入口：`X509ExtensionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
