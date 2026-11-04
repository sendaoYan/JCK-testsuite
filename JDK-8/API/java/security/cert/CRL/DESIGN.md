# java.security.cert.CRL 黑盒行为测试设计

## 1. API 功能
抽象类，仅暴露 `getType()`（final）/`isRevoked(Certificate)`/`toString()` 三个表层方法；其余（getEncoded/getThisUpdate 等）在 X509CRL 子类上。

## 2. Java 用户如何使用
用户通过 `CertificateFactory.generateCRL` 拿到具体 X509CRL 实例，经基类 `getType()/isRevoked()` 查询吊销状态。

## 3. 开源代码实际用法调研
jdk8u-dev `test/java/security/cert/`（X509CRL 相关用例）；基类只测类型/吊销判定/toString。

## 4. 测试用例设计 + 自动化实现
固定夹具（同目录测试文件内嵌 base64）：keytool 生成的 RSA-2048 自签证书 CN=jck-root（有效期 2026-11-03..2036-10-31，SHA384withRSA）与 jck-root 签发的 v1 CRL。运行时用 `CertificateFactory` 解码，无外部文件/网络。

- 白盒：public abstract class、extends Object、声明 getType/isRevoked/toString、getType 为 final。
- 黑盒：真实 X509CRL 的 `getType()=="X.509"`、未吊销证书 `isRevoked()==false`、`toString()` 非空；自定义 `CRL` 子类验证受保护 type 构造器 + 三个抽象方法的派发。

断言入口：`CRLTest.java` 的 `testBlackBox()`（白盒在 `testWhiteBox()`，保留原有）。
