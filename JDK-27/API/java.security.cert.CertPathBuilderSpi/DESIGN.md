# java.security.cert.CertPathBuilderSpi 黑盒行为测试设计

## 1. API 功能
provider SPI：engineBuild 抽象方法。

## 2. Java 用户如何使用
provider 作者实现；测试用最小实现驱动。

## 3. 开源代码实际用法调研
sun.security.provider.certpath.SunCertPathBuilder（jdk27 src）。

## 4. 测试用例设计 + 自动化实现
黑盒：匿名 Spi 经 provider 暴露后 engineBuild 收到参数并返回（记录调用）；直接 new 驱动抽象性（不可实例化）与子类覆盖。

断言入口：`CertPathBuilderSpiTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
