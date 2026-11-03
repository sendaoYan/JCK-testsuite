# java.security.cert.PKIXParameters 黑盒行为测试设计

## 1. API 功能
PKIX 验证参数：trustAnchors/initialPolicies/certStores/revocation 与四个策略开关/qualifiersRejected/date/sigProvider/targetCertConstraints/certPathCheckers + ctor(Set<TrustAnchor>)/ctor(KeyStore) + clone。

## 2. Java 用户如何使用
用户配置验证策略的标准载体。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/PKIXParameters/InvalidParameters.java。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：Set 构造与 KeyStore 构造（PKCS12 只读证书条目转 anchor）；revocation 默认 true、四策略开关默认 false/true 逐一往返；setDate 精确读回（null 表示当前时间）；initialPolicies/certStores(add/set)/sigProvider/targetCertConstraints/certPathCheckers(set/add) 全部往返；clone 深拷贝行为；空 anchors 抛 InvalidAlgorithmParameterException、null anchors NPE。

断言入口：`PKIXParametersTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
