# java.security.KeyStore 黑盒行为测试设计

## 1. API 功能
- 工厂：`getInstance(type[, provider|providerName])`、`getInstance(File, char[]|LoadStoreParameter)`、`getDefaultType()`
- 装载/存储：`load(InputStream, char[])`、`load(LoadStoreParameter)`、`store(OutputStream, char[])`、`store(LoadStoreParameter)`
- 条目操作：`setKeyEntry`（Key 或字节数组）、`setCertificateEntry`、`setEntry`、`deleteEntry`
- 查询：`getKey`、`getCertificateChain`、`getCertificate`、`getCertificateAlias`、`getEntry`、`aliases`、`containsAlias`、`size`、`isKeyEntry`、`isCertificateEntry`、`entryInstanceOf`、`getCreationDate`、`getCreationInstant`、`getAttributes(String)`

## 2. Java 用户如何使用
标准三步：`getInstance("PKCS12") → load(流, 密码) → setKeyEntry/setCertificateEntry → store`；或 `getInstance(File, password)` 一步探测装载。getCertificateAlias 按 alias 迭代顺序返回首个命中（链条目优先于后插入的证书条目）。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyStore.java`（KeyStore 门面 + 委派 KeyStoreSpi）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/TestKeyStoreBasic.java`（load/store/getKey 全链路、错口令异常）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/EntryMethods.java`（Entry API + ProtectionParameter）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/sun/security/provider/KeyStore/DKSTest.java`（LoadStoreParameter 装载）

## 4. 测试用例设计 + 自动化实现
- 正常：PKCS12 内存库 私钥+可信证书 完整生命周期（set/get/分类/别名/大小/删除/日期）
- 组合：store→reload 往返（证书 equals、私钥 equals）；getCertificateAlias 首命中顺序；entryInstanceOf；getCreationDate 与 getCreationInstant 一致
- 异常：错密钥口令 UnrecoverableKeyException、错库口令 IOException("keystore password was incorrect")、未加载先操作 KeyStoreException、未知类型 KeyStoreException、null 别名 NPE、null Key KeyStoreException
- 行为记录：PKCS12 getEntry 合成 friendlyName(1.2.840.113549.1.9.20)/localKeyID 属性；getAttributes(未知类型) 空

断言入口：`KeyStoreTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
