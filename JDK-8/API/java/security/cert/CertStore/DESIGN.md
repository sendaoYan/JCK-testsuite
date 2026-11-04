# java.security.cert.CertStore 黑盒行为测试设计

## 1. API 功能
`getInstance(type,params[,provider])`/`getCertificates(selector)`/`getCRLs(selector)`/`getType`/`getProvider`/`getCertStoreParameters`/`getDefaultType`。

## 2. Java 用户如何使用
以 Collection/LDAP/URI 后端向 PKIX 提供交叉证书与 CRL。

## 3. 开源代码实际用法调研
jdk8u-dev `test/java/security/cert/CertPathBuilder/`（CollectionCertStoreParameters 用法）。

## 4. 测试用例设计 + 自动化实现
固定夹具（同目录测试文件内嵌 base64）：RSA-2048 自签证书 CN=jck-root / CN=jck-leaf 与 jck-root 签发的 v1 CRL。运行时解码，无外部文件/网络。

- 白盒：getInstance/getType/getProvider/getCertStoreParameters、空 store 的 getCertificates/getCRLs 为空集合、getDefaultType 非空。
- 黑盒：Collection store 注入两证书一 CRL；null selector 返回全部（证书 2/CRL 1）且返回集合是独立副本（clear 不影响 store）；X509CertSelector 按 subject 过滤命中；X509CRLSelector 按 issuer 过滤命中；`getInstance(type,params,provider)` 与 provider-name 重载；未知 provider 抛 NoSuchProviderException、未知 type 抛 NoSuchAlgorithmException、LDAP 配 Collection 参数抛 InvalidAlgorithmParameterException。

断言入口：`CertStoreTest.java` 的 `testBlackBox()`（白盒在 `testWhiteBox()`，保留原有）。
