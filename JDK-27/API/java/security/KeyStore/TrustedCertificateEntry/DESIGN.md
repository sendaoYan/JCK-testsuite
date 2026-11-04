# java.security.KeyStore.TrustedCertificateEntry 黑盒行为测试设计

## 1. API 功能
- 构造：`TrustedCertificateEntry(Certificate)`、`TrustedCertificateEntry(Certificate, Set<Attribute>)`
- 查询：`getTrustedCertificate`、`getAttributes`、`toString`

## 2. Java 用户如何使用
信任锚条目（无密钥）。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyStore.java`（无 equals 覆写→恒等语义）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/PKCS12/MetadataStoreLoadTest.java`

## 4. 测试用例设计 + 自动化实现
- 正常：证书引用往返、属性保留
- 组合：PKCS12 setEntry(null protection)/getEntry 往返并分类为证书条目
- 行为记录：恒等 equals
- 异常：null 证书 → NPE

断言入口：`TrustedCertificateEntryTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。


## 5. 契约深化补充（testContractDeep）
- JKS null 保护闭环：分类、getCertificate equals、null 证书链、getKey null、getCertificateAlias 命中。
- 口令保护双向拒绝：setEntry → KeyStoreException；getEntry → UnsupportedOperationException。
- 覆盖+删除生命周期（同 alias 覆盖 size 不变；delete 后 getEntry null）；JKS 往返后属性集为空。
