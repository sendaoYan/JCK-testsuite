# java.security.cert.CertPathChecker 黑盒行为测试设计

## 1. API 功能
JDK 8+ 检查器接口：init(boolean)/isForwardCheckingSupported/getSupportedExtensions/check(Certificate,Collection)。

## 2. Java 用户如何使用
用户实现自定义链检查（如私有扩展校验）后加入 PKIXParameters。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathValidator/OCSP/* 与自定 checker 用例。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：实现记录 init/check 调用，经 PKIXParameters.setCertPathCheckers 注入后真实 PKIX 验证会调用它（root 链上观察到 init(false)+check）；isForwardCheckingSupported=false 走逆向。

断言入口：`CertPathCheckerTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
