# java.security.cert.X509CRLSelector 黑盒行为测试设计

## 1. API 功能
CRL 选择器：setIssuers/addIssuer*/setMinCRLNumber(实际为 setMinCRL/setMaxCRL——JDK-27 实测签名见 javap)/setDateAndTime/setCertificateChecking + match/getters/clone。

## 2. Java 用户如何使用
限定取哪个 CA、哪个时段的 CRL。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/X509CRLSelectorTest.java（经典用例，setter/getter 全覆盖）。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：空 selector match(fixture CRL)；按 issuer 过滤命中/排除；getMinCRL/getMaxCRL、setCertificateChecking 往返；match(null)==false；clone 内容相等且独立；toString 非空。

断言入口：`X509CRLSelectorTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
