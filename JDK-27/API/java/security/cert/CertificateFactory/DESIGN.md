# java.security.cert.CertificateFactory 黑盒行为测试设计

## 1. API 功能
证书工厂：generateCertificate(s)/generateCRL(s)/generateCertPath、getCertPathEncodings、provider/algorithm/type 访问器。

## 2. Java 用户如何使用
用户从流/PKCS#7 文件解码 X.509 证书、CRL 与证书链；是 JSSE 与 keytool 的基础。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertificateFactory/*.java（如 CheckCase、GenerateCertificate、PKCS7）大量使用。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：getInstance("X.509") 单例语义（同一算法+provider 返回不同实例但等价）；generateCertificate 两份解码 equals；generateCertPath(List) 长度与类型；generateCertPath 的默认编码 P 与 getCertPathEncodings 迭代有序；generateCRLs 对非 CRL 输入抛 CRLException；null 流抛 NPE/CertificateException（JDK-27 实测抛异常而非返回 null）；getProvider/getAlgorithm/getType。

断言入口：`CertificateFactoryTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
