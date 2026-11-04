# java.security.cert.CertificateFactorySpi 黑盒行为测试设计

## 1. API 功能
provider SPI 基类：engineGenerateCertificate/CRL/CertPath 等抽象方法 + 默认 engineGenerateCertificates 实现。

## 2. Java 用户如何使用
加密 provider 作者实现；应用用户不直接使用。测试用最小实现驱动模板方法行为。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk 无直接引用；sun.security.provider.X509Factory 为 JDK 内实现（jdk27 src）。

## 4. 测试用例设计 + 自动化实现
黑盒：自定义 Spi 子类实现 engineGenerateCertificate，经 provider 注册后 CertificateFactory.getInstance 走到该实现（或直接以匿名子类验证模板 engineGenerateCertificates 逐个调用 engineGenerateCertificate、坏元素抛 CertificateException）；getProvider 传播。

断言入口：`CertificateFactorySpiTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
