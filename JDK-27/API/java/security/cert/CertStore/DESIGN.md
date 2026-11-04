# java.security.cert.CertStore 黑盒行为测试设计

## 1. API 功能
getInstance(type,params)/getCertificates/getCRLs/getCertStoreType/getProvider/getType + getDefaultType。

## 2. Java 用户如何使用
用户以 Collection/LDAP/URI 后端向 PKIX 提供交叉证书与 CRL。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathBuilder/NoExtensions.java（CollectionCertStoreParameters 用法）。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：getInstance 参数校验；Collection store 注入 root+leaf 后 getCertificates(selector) 过滤命中（X509CertSelector 按证书）；getCRLs(X509CRLSelector) 命中 fixture CRL；返回集合不可变（add 抛 UOE）；"Collection"/"LDAP"/"URI" 类型与 getDefaultType=="CertStore"。

断言入口：`CertStoreTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
