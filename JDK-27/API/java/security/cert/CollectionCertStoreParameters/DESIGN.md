# java.security.cert.CollectionCertStoreParameters 黑盒行为测试设计

## 1. API 功能
集合后端参数：ctor()/ctor(Collection)，getCollection/clone/toString/equals/hashCode。

## 2. Java 用户如何使用
最常用后端：把内存中的证书/CRL 提供给 PKIX。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathBuilder/NoExtensions.java。

## 4. 测试用例设计 + 自动化实现
固定夹具（FX_ALL/FX_CRL，同目录测试文件内嵌 base64 文本）：keytool 生成的 RSA-2048 自签证书 CN=jck-root / CN=jck-leaf（有效期 2026-11-03..2036-10-31，SHA384withRSA，root serial 0x70bdab2c498601da）及 root 签发的 v1 CRL（thisUpdate 2026-11-03，nextUpdate 2027-02-01，已吊销 serial 0），运行时解码，无外部文件。 黑盒：空 ctor getCollection 为空且后续可变（JDK-27 实测返回可写集合）；带集合 ctor 保留内容、clone 复制集合、equals/hashCode 按内容；不可变集合传入后 add 抛 UOE 传播。

断言入口：`CollectionCertStoreParametersTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
