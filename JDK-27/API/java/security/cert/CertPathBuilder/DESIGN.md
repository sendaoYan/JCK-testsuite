# java.security.cert.CertPathBuilder 黑盒行为测试设计

## 1. API 功能
getInstance(type)/getInstance(type,provider)/build(params) 门面 + getDefaultType + getRevocationChecker 通路。

## 2. Java 用户如何使用
用户按目标选择器构造信任链（如 TLS 客户端按 SAN 选证书）。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathBuilder/NoExtensions.java、targetConstraints/BuildEEBasicConstraints.java。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：getInstance 参数校验（null/未知 type 抛 NPE/NoSuchAlgorithmException）；build 以 target==anchor 得到空链+有效 TrustAnchor 的 PKIXCertPathBuilderResult；getRevocationChecker 返回 PKIXRevocationChecker；getDefaultType 为 "PKIX"；带 CertStore 的构建。

断言入口：`CertPathBuilderTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
