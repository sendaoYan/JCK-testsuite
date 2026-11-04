# java.security.KeyStore.SecretKeyEntry 黑盒行为测试设计

## 1. API 功能
- 构造：`SecretKeyEntry(SecretKey)`、`SecretKeyEntry(SecretKey, Set<Attribute>)`
- 查询：`getSecretKey`、`getAttributes`、`toString`

## 2. Java 用户如何使用
对称密钥条目。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyStore.java`（无 equals 覆写→恒等语义；toString 报算法名）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/EntryMethods.java`

## 4. 测试用例设计 + 自动化实现
- 正常：key 引用往返、toString 含 "AES"、属性构造保留
- 组合：PKCS12 setEntry/getEntry 往返密钥字节一致
- 行为记录：恒等 equals（同 key 的新实例不等）
- 异常：null key → NPE；null 属性集合 → NPE

断言入口：`SecretKeyEntryTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。


## 5. 契约深化补充（testContractDeep）
- JKS setKeyEntry(SecretKey) → KeyStoreException（PrivateKey-only 格式）。
- JCEKS 全闭环：分类（isKeyEntry true/非证书条目）、null 证书链、getEntry 类型往返、encoded 字节往返、getKey 算法、错口令 UnrecoverableKeyException、deleteEntry 生命周期（getEntry 后续 null）。
