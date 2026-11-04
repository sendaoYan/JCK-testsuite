# java.security.cert.X509CertSelector 黑盒行为测试设计

## 1. API 功能
证书选择器：setCertificate/setSerialNumber/setIssuer/setSubject/setSubjectKeyIdentifier/setSubjectPublicKey/setCertificateValid/setBasicConstraints/setKeyUsage/setExtendedKeyUsage/setSubjectAlternativeNames/setNameConstraints/setPolicy/setPathToNames 等 + match/clone/getters。

## 2. Java 用户如何使用
`CertStore.getCertificates` 与 `PKIXBuilderParameters` target 的标准过滤方式。

## 3. 开源代码实际用法调研
jdk8u-dev `test/java/security/cert/X509CertSelectorTest.java`。

## 4. 测试用例设计 + 自动化实现
固定夹具（同目录测试文件内嵌 base64）：RSA-2048 自签证书 CN=jck-root（CA:TRUE，含 SKI）/ CN=jck-leaf（非 CA）。运行时解码，无外部文件/网络。

- 白盒：默认值（getBasicConstraints==-1、getMatchAllSubjectAltNames==true、getSubjectPublicKeyAlgID/getNameConstraints==null）+ serial/certificateValid/clone。
- 黑盒：空 selector 匹配一切但 null 不匹配；setCertificate 钉住精确匹配；serial/issuer/subject（Principal 与 byte[]）/subjectPublicKey（PublicKey 与 byte[]）/subjectPublicKeyAlgID/subjectKeyIdentifier/authorityKeyIdentifier/certificateValid/privateKeyValid/keyUsage/extendedKeyUsage/SAN（set+add）/nameConstraints（空 NameConstraints DER）/policy/pathToNames（set+add）全部往返；basicConstraints（0 匹配 CA、-2 匹配非 CA、-1 为无约束）；clone 深拷贝独立；null 清空语义；非法 DER/OID 抛 IOException（setIssuer/setSubject/setSubjectPublicKey/setExtendedKeyUsage/setPolicy）。

断言入口：`X509CertSelectorTest.java` 的 `testBlackBox()`（白盒在 `testWhiteBox()`，保留原有）。
