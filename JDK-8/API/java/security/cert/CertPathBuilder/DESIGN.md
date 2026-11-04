# java.security.cert.CertPathBuilder 黑盒行为测试设计

## 1. API 功能
`getInstance(type[,provider])`/`build(params)` 门面 + `getDefaultType` + `getRevocationChecker`。

## 2. Java 用户如何使用
用户按目标选择器构建信任链。

## 3. 开源代码实际用法调研
jdk8u-dev `test/java/security/cert/CertPathBuilder/`（NoExtensions、targetConstraints 等）。

## 4. 测试用例设计 + 自动化实现
固定夹具（同目录测试文件内嵌 base64）：RSA-2048 自签证书 CN=jck-root。运行时解码，无外部文件/网络。

- 白盒：getInstance/getAlgorithm/getProvider/getRevocationChecker/getDefaultType。
- 黑盒：getInstance("PKIX") 与 provider/provider-name 重载；未知算法抛 NoSuchAlgorithmException、未知 provider 抛 NoSuchProviderException；getDefaultType=="PKIX"；getRevocationChecker 为 PKIXRevocationChecker 且无默认 options；build（target==anchor）得到 PKIXCertPathBuilderResult，**目标即信任锚时路径为空**（PKIX 语义），trustAnchor/publicKey 正确、policyTree 非空；不可达 target 抛 CertPathBuilderException；build(null) 抛 InvalidAlgorithmParameterException。

断言入口：`CertPathBuilderTest.java` 的 `testBlackBox()`（白盒在 `testWhiteBox()`，保留原有）。
