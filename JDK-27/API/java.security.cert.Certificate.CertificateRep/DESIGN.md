# java.security.cert.Certificate.CertificateRep 黑盒行为测试设计

## 1. API 功能
Certificate 的序列化代理（protected static 内部类）：持 type+encoded，readResolve 还原为 CertificateFactory 解码出的证书。

## 2. Java 用户如何使用
一般不直接使用；用户在序列化证书时隐式经由 writeReplace 触达。测试中用同包不可行（default 包测试），改为通过外层对象的序列化行为覆盖。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk 中证书序列化回归（test/jdk/java/security/cert/...）依赖该代理保证反序列化兼容。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒（经外层）：root 证书序列化→反序列化得到 X509Certificate，type/编码/equals 与原一致；leaf 同样成立；两次往返结果彼此 equals。

断言入口：`CertificateRepTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
