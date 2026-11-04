# java.security.cert.X509Certificate 黑盒行为测试设计

## 1. API 功能
X.509 证书读取面：版本/序列号/主体/签发者/有效期/签名算法/密钥用法/基本约束/SAN/EKU/唯一 ID/verify/getTBSCertificate + X509Extension + equals。

## 2. Java 用户如何使用
TLS 信任、代码签名验证、KeyStore 证书条目的基础类型。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/X509Certificate/*.java 与 test/jdk/javax/net/ssl/ 大量使用。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：v3、serial 0x70bdab2c498601da、subject/issuer CN=jck-root（RFC2253/legacy DN 双视图）、SHA384withRSA+OID、checkValidity() 现在通过且 2020-01-01 抛 NotYetValid、KeyUsage==null（fixture 无该扩展）、basicConstraints==Integer.MAX_VALUE（CA）、critical 扩展恰为 {2.5.29.19}、non-critical 含 2.5.29.14、SAN/EKU/唯一 ID 均为 null、verify 自身通过/错钥 SignatureException、两份解码 equals/hashCode 相等。

断言入口：`X509CertificateTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
