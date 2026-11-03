# java.security.cert.CertPathValidatorSpi 黑盒行为测试设计

## 1. API 功能
provider SPI：engineValidate/engineGetRevocationChecker 抽象 + 默认。

## 2. Java 用户如何使用
provider 作者实现；测试以最小子类驱动。

## 3. 开源代码实际用法调研
sun.security.provider.certpath.PKIXCertPathValidator（jdk27 src）。

## 4. 测试用例设计 + 自动化实现
黑盒：子类 engineValidate 记录参数返回 null/结果；默认 engineGetRevocationChecker 抛 UnsupportedOperationException（JDK-27 实测）；抽象方法不可直接实例化。

断言入口：`CertPathValidatorSpiTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
