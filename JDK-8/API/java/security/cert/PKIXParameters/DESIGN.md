# java.security.cert.PKIXParameters 黑盒行为测试设计

## 1. API 功能
PKIX 验证参数：trustAnchors/initialPolicies/certStores/revocation 与四个策略开关/qualifiersRejected/date/sigProvider/targetCertConstraints/certPathCheckers + 两个构造器（Set<TrustAnchor> 与 KeyStore）+ clone。

## 2. Java 用户如何使用
用户配置 PKIX 验证策略的标准载体。

## 3. 开源代码实际用法调研
jdk8u-dev `test/java/security/cert/PKIXParameters/InvalidParameters.java`。

## 4. 测试用例设计 + 自动化实现
固定夹具（同目录测试文件内嵌 base64）：RSA-2048 自签证书 CN=jck-root。运行时解码，无外部文件/网络。

- 白盒：Set 构造 + 默认值（revocation true、date null）+ setDate 克隆。
- 黑盒：Set 构造与 KeyStore 构造（PKCS12 证书条目转 anchor；空 keystore 抛 InvalidAlgorithmParameterException）；revocation 默认 true、四个策略开关/qualifiersRejected 逐一往返；setDate 克隆读回；initialPolicies/certStores/sigProvider/targetCertConstraints/certPathCheckers 全部往返；**不可变视图**（getTrustAnchors/getInitialPolicies/getCertStores/getCertPathCheckers 的 add/clear 抛 UnsupportedOperationException）；setInitialPolicies/setCertStores/setCertPathCheckers(null) 清空；空 anchors 抛 InvalidAlgorithmParameterException、null anchors 抛 NPE；clone 深拷贝独立。

断言入口：`PKIXParametersTest.java` 的 `testBlackBox()`（白盒在 `testWhiteBox()`，保留原有）。
