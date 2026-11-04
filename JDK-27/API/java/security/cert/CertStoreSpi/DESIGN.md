# java.security.cert.CertStoreSpi 黑盒行为测试设计

## 1. API 功能
provider SPI：engineGetCertificates/engineGetCRLs 抽象。

## 2. Java 用户如何使用
provider 作者实现。

## 3. 开源代码实际用法调研
sun.security.provider.certpath.*（jdk27 src）。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：子类返回 fixture 证书集合，经 provider/直接调用验证 selector 过滤委托给实现（selector 匹配逻辑由 Collection store 在外部做——实测 Collection 实现在 Spi 外过滤，断言子类收到 selector 引用并返回全量）。

断言入口：`CertStoreSpiTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
