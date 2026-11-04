# CertificateFactory — 设计案

## 1. API 功能
证书/CRL/CertPath 工厂。getInstance 工厂 + generateCertificate/generateCertificates/generateCRL/generateCertPath(List/InputStream)/getType/getProvider。

## 2. 使用方式
`CertificateFactory.getInstance("X.509").generateCertificate(DER 流)`。

## 3. 参考来源
- javadoc `java.security.cert.CertificateFactory`。
- OpenJDK `test/jdk/java/security/cert/CertificateFactory/`。
- JCK runtime-8b `api/java_security/cert/`。

## 4. 测试用例设计 + 易错规范细节
- 工厂矩阵：bogus→CertificateException；null→NPE；null Provider/String→IAE；bogus provider→NoSuchProviderException。
- generateCertificate(DER) 解析；generateCertificate(null)/generateCertificates(null)→CertificateException；generateCertificates 集合数量；generateCRL(证书 DER)→CRLException；generateCertPath(List) 类型与数量；generateCertPath(null List)→NPE。
- getType "X.509"、getProvider 非空。
