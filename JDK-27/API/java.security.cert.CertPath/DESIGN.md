# java.security.cert.CertPath 黑盒行为测试设计

## 1. API 功能
证书链抽象类：getCertificates、getEncoded(String)/默认编码、getEncodings、getType、equals（编码比较）+hashCode、writeReplace。

## 2. Java 用户如何使用
用户由 CertificateFactory.generateCertPath 或 CertPathBuilder 得到，交 CertPathValidator 验证。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathBuilder/* 与 CertPathValidator/* 全量使用；CertPathRep 往返见序列化用例。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：root 单证书链与 [root,leaf] 链编码/equals/hashCode 差异；getEncodings 迭代器首个为默认编码 P；getEncoded("P") 非空且与 regenerate 后 equals；序列化往返类型与长度保持；getType 为 X.509。

断言入口：`CertPathTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
