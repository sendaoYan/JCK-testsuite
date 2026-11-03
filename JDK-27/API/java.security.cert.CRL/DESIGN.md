# java.security.cert.CRL 黑盒行为测试设计

## 1. API 功能
CRL 抽象类：getType/isRevoked/toString + 抽象 getEncoded/getNextUpdate/getThisUpdate/getRevokedCertificates/verify/getSigAlgName 等。

## 2. Java 用户如何使用
用户经 CertificateFactory.generateCRL 获得，查询吊销状态。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/X509CRL/*。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：v1 CRL 的 thisUpdate/nextUpdate 固定值、type=="X.509"、isRevoked(已吊销证书 serial 0)==true / 未吊销 false、getRevokedCertificates 非空集合、verify(root 公钥) 通过+错钥抛异常、getSigAlgName=="SHA384withRSA"、getEncoded 非空。

断言入口：`CRLTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
