# java.security.cert.Certificate 黑盒行为测试设计

## 1. API 功能
证书抽象基类：equals（编码比较）/hashCode（DER 的十六进制字符串 hashCode）、getEncoded、verify(pubKey)（抽象）、getPublicKey、toString、writeReplace 序列化代理。

## 2. Java 用户如何使用
用户通过 CertificateFactory 生成实例，用于 SSL/TLS 信任链、代码签名、KeyStore 存取；equals 按编码比较两份独立解码的证书。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertificateFactory/*.java 用 generateCertificate 对比编码；CertificateRep 经 ObjectOutputStream 往返出现在多个序列化用例（test/jdk/java/security/.../Serial*）。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：两份独立解码的 root 证书 equals/hashCode 相等且与 leaf 不等；getEncoded/TBS 内容非空且稳定；verify 用自身公钥通过、用新生成的 RSA 公钥抛 SignatureException；经 ObjectOutputStream 序列化往返 getEncoded 不变（writeReplace→CertificateRep）；toString 非空。

断言入口：`CertificateTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
