# java.security.cert.X509CertSelector 黑盒行为测试设计

## 1. API 功能
证书选择器：setCertificate/setSerialNumber/setIssuer/setSubject/setKeyUsage/setBasicConstraints/setCertificateValid/addIssuer + match/getters/clone。

## 2. Java 用户如何使用
CertStore.getCertificates 与 PKIXBuilderParameters target 的标准过滤方式。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/X509CertSelectorTest.java（全 setter 用例）。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：空 selector 匹配一切；setCertificate 钉住后 root 命中/leaf 排除/null 排除；serial 过滤精确；issuer/subject Principal 过滤；setCertificateValid(2030) 命中、(2020) 排除；全 false 的 keyUsage 数组等于无约束；clone 独立且行为一致。 追加 testSelectorRoundTrips：subjectPublicKey(PublicKey/byte[])/subjectPublicKeyAlgID/subjectKeyIdentifier/authorityKeyIdentifier/privateKeyValid/keyUsage/extendedKeyUsage/SAN/pathToNames/nameConstraints/policy 全字段往返、clone 深拷贝独立、null 清空语义、非法 DER/OID 抛 IOException、issuer/subject byte[] 往返。

断言入口：`X509CertSelectorTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
