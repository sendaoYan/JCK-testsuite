# java.security.cert.CertPathValidator 黑盒行为测试设计

## 1. API 功能
getInstance(type)/getInstance(type,provider)/validate(path,params) 门面 + getDefaultType + getRevocationChecker。

## 2. Java 用户如何使用
TLS/JSSE 路径验证的入口。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathValidator/*（含 OCSP/GetAndPostTests.java、trustAnchor/ValWithAnchorByName.java）。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：getInstance 参数校验；validate(root 链, anchor=root) 成功返回 PKIXCertPathValidatorResult（无策略扩展时 policyTree 为 null）；错 anchor 抛 CertPathValidatorException；getRevocationChecker 类型；getDefaultType 为 "PKIX"。 追加：validate(路径,null 参数) 抛 InvalidAlgorithmParameterException。

断言入口：`CertPathValidatorTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
