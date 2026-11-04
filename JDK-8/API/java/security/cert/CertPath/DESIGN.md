# CertPath — 设计案

## 1. API 功能
证书路径抽象类。getType/getCertificates/getEncoded()/getEncoded(String)。

## 2. 使用方式
`CertificateFactory.generateCertPath(List)` 构建；`getEncoded("PkiPath")/("PKCS7")` 编码；`generateCertPath(InputStream)` 回读 PkiPath。

## 3. 参考来源
- javadoc `java.security.cert.CertPath`。
- OpenJDK `test/jdk/java/security/cert/`。
- JCK runtime-8b `api/java_security/cert/CertPath/`。

## 4. 测试用例设计 + 易错规范细节
- 抽象类层级；getType "X.509"；getCertificates 数量与元素。
- 编码：getEncoded() 非空、PkiPath/PKCS7 非空；PkiPath 流回读往返。
- 异常：getEncoded(null)→NPE；getEncoded(bogus)→CertificateEncodingException。
- 易错点：getCertificates 返回 List<? extends Certificate>（编译期只读）；X.509 CertPath 仅支持 PkiPath 流回读，PKCS7 编码不可用 generateCertPath(InputStream) 回读。
