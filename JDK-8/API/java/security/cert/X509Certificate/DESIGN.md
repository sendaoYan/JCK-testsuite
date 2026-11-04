# X509Certificate — 设计案

## 1. API 功能
X.509 证书抽象类（extends Certificate）。getVersion/getSerialNumber/getSigAlgName/getSigAlgOID/getNotBefore/getNotAfter/getSubjectDN/getIssuerDN/getSubjectX500Principal/getIssuerX500Principal/getBasicConstraints/checkValidity/verify/getEncoded/getType。

## 2. 使用方式
`CertificateFactory.getInstance("X.509").generateCertificate(DER 输入流)` 解析固定自签证书。

## 3. 参考来源
- javadoc `java.security.cert.X509Certificate`。
- OpenJDK `test/jdk/java/security/cert/`。
- JCK runtime-8b `api/java_security/cert/`。

## 4. 测试用例设计 + 易错规范细节
- 嵌入固定 DER 自签证书（RSA2048/SHA256withRSA/serial 0x0f17ae3a），解析后断言 version=3、serial、sigAlgName/SigAlgOID、公钥算法 RSA、有效期、DN（用 X500Principal RFC2253 无空格）、basicConstraints=-1。
- checkValidity(Date) 用证书自身 notBefore/notAfter 的 ±1000ms 构造确定日期：界内不抛、界外抛 CertificateNotYetValid/ExpiredException（不依赖墙钟）。
- verify(self public key) 通过；getEncoded 重解析 equals；equals/hashCode/toString。
- 易错点：避免无参 checkValidity()（依赖当前墙钟）；notBefore 为 2026-11-04。
