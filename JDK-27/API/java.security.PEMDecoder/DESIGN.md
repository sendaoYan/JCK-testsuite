# java.security.PEMDecoder 黑盒行为测试设计

## 1. API 功能
- 工厂：`of()`（共享实例）
- 操作：`decode(String)`/`decode(Reader)` → BinaryEncodable

## 2. Java 用户如何使用
按 "-----BEGIN X-----" 标签路由：CERTIFICATE→X509Certificate、PRIVATE KEY→PrivateKey、未知标签→PEM、垃圾输入→IAE。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/PEMDecoder.java`（preview）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/PEMDecoder/`（各类型解码断言）

## 4. 测试用例设计 + 自动化实现
- 正常：固定自签根证书（内嵌 base64，keytool 预生成）解码为 X509Certificate 且与 CertificateFactory 结果 equals；PKCS#8 私钥解码 getFormat=PKCS#8 且模数与证书公钥一致；CRL 标签 → X509CRL
- 组合：PEMEncoder 产物再解码 equals
- 异常：非 PEM 文本 IAE；of() 返回共享实例（JDK-27 实测）

断言入口：`PEMDecoderTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
