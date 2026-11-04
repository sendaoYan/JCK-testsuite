# java.security.cert.PKIXBuilderParameters 黑盒行为测试设计

## 1. API 功能
PKIX 构建参数：继承 PKIXParameters + maxPathLength（默认 5，-1 为无约束，<-1 抛 IAE）+ ctor(Set,CertSelector)/ctor(KeyStore,CertSelector)。

## 2. Java 用户如何使用
构建时的深度与目标约束。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathBuilder/targetConstraints/BuildEEBasicConstraints.java。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：默认 5、0、-1、正数往返与越界抛 IAE；null selector 允许；instanceof PKIXParameters；继承开关生效；空 anchors 抛 IAE；KeyStore 构造 anchors+默认 5。

断言入口：`PKIXBuilderParametersTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
