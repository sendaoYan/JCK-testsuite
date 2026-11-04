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


## 5. 契约深化补充（testContractDeep）
- 构造校验链补全：零长链 IAE、链内 null NPE、证书 type 混合 IAE、私钥/公钥算法不匹配 IAE（FauxPrivateKey+真证书触发）。
- 链防御性拷贝双向（构造后改源数组、取回后改泄漏数组均无影响）。
- JKS setKeyEntry 端到端闭环（真 RSA 私钥+双证书链，跨实例 equals）；setKeyEntry null chain → IllegalArgumentException。
