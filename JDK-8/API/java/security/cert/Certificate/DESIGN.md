# Certificate — 设计案

## 1. API 功能
证书抽象类（implements Serializable）。getType/getPublicKey/getEncoded/verify(PublicKey)/verify(PublicKey,provider)/hashCode/equals/toString。

## 2. 使用方式
通过 CertificateFactory 解析 X.509 证书得到具体实现。

## 3. 参考来源
- javadoc `java.security.cert.Certificate`。
- JCK runtime-8b `api/java_security/cert/Certificate/`。

## 4. 测试用例设计 + 易错规范细节
- 抽象类 + Serializable。
- 具体 X.509：getType/getPublicKey/getEncoded/verify(公钥)/verify(公钥,provider)/hashCode/equals/toString。
- 序列化往返 equals 且 getEncoded 相等。
- 易错点：verify(公钥,"SUN") 失败（SHA256withRSA 在 SunRsaSign），用 "SunRsaSign"；与 java.security.Certificate 歧义需全限定。
