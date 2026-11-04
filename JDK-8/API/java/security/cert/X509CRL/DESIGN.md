# java.security.cert.X509CRL 黑盒行为测试设计

## 1. API 功能
X.509 CRL 读取面：版本/签发者/sig 算法/this/next update/吊销集合/TBSCertList/签名/verify/getRevokedCertificate 重载 + X509Extension 扩展契约。

## 2. Java 用户如何使用
`CertificateFactory.generateCRL` 解析后做吊销检查与验签。

## 3. 开源代码实际用法调研
jdk8u-dev `test/java/security/cert/X509CRL/`。

## 4. 测试用例设计 + 自动化实现
固定夹具（同目录测试文件内嵌 base64）：keytool 生成的 RSA-2048 自签证书 CN=jck-root（SHA384withRSA，serial 0x70bdab2c498601da）与 jck-root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，吊销 serial 0）。运行时解码，无外部文件/网络。

- 白盒：public abstract class extends CRL implements X509Extension（JDK-8 **不** implements `BinaryEncodable`）；声明 equals/getEncoded/getIssuerDN/getIssuerX500Principal/getNextUpdate/getRevokedCertificate×2/getRevokedCertificates/getSigAlgName/getSigAlgOID/getSigAlgParams/getSignature/getTBSCertList/getThisUpdate/getVersion/hashCode/verify。
- 黑盒：版本/签发者 Principal 与 X500Principal/`SHA384withRSA`/sig OID/sig 参数/thisUpdate/nextUpdate 先后；吊销 serial 0 命中、serial 1 未命中、`getRevokedCertificate(cert)` 重载、revocation reason 解析；`getTBSCertList()`/`getSignature()` 非空；编码往返 equals+hashCode；`verify(签发公钥)` 成功、错钥抛受检异常；X509Extension 无扩展时返回 null 集合 + `getExtensionValue` 未知 OID 为 null。

断言入口：`X509CRLTest.java` 的 `testBlackBox()`（白盒在 `testWhiteBox()`，保留原有）。
