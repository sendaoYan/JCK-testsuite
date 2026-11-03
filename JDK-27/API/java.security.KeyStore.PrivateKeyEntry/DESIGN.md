# java.security.KeyStore.PrivateKeyEntry 黑盒行为测试设计

## 1. API 功能
- 构造：`PrivateKeyEntry(PrivateKey, Certificate[])`、`PrivateKeyEntry(PrivateKey, Certificate[], Set<Attribute>)`
- 查询：`getPrivateKey`、`getCertificateChain`、`getCertificate`、`getAttributes`、`toString`

## 2. Java 用户如何使用
私钥+证书链条目；链首为实体证书。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyStore.java`（链克隆、null 校验、无 equals 覆写→恒等语义）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/PKCS12/MetadataStoreLoadTest.java`

## 4. 测试用例设计 + 自动化实现
- 正常：key/chain/leaf 读取、链读侧克隆、属性集合保留
- 组合：PKCS12 setEntry/getEntry 往返（key equals、cert equals）
- 行为记录：JDK-27 无 equals 覆写——同内容两个实例不相等（恒等语义）
- 异常：null key/null 链/null 链元素 → NPE；空链 → IllegalArgumentException

断言入口：`PrivateKeyEntryTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
