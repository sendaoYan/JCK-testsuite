# java.security.KeyStoreSpi 黑盒行为测试设计

## 1. API 功能
- 抽象引擎：`engineGetKey`、`engineGetCertificateChain`、`engineGetCertificate`、`engineGetCreationDate`、`engineSetKeyEntry`×2、`engineSetCertificateEntry`、`engineDeleteEntry`、`engineAliases`、`engineContainsAlias`、`engineSize`、`engineIsKeyEntry`、`engineIsCertificateEntry`、`engineGetCertificateAlias`、`engineStore`、`engineLoad`
- 具体默认：`engineGetCreationInstant`（委托 Date）、`engineStore/engineLoad(LoadStoreParameter)`、`engineGetAttributes`、`engineGetEntry`、`engineSetEntry`、`engineEntryInstanceOf`、`engineProbe`

## 2. Java 用户如何使用
Provider 实现 KeyStore 类型的核心 SPI；KeyStore 门面全部 final 方法都委派到这里。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyStoreSpi.java`（默认实现与抽象集）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/`（多组对 SPI 行为的间接验证）

## 4. 测试用例设计 + 自动化实现
- 黑盒：LinkedHashMap 支持的具体子类 MapSpi 驱动全部引擎方法——可信证书路径（set/size/contains/分类/别名/日期/instant 委派/entry API）、私钥路径（getKey/chain）、别名插入顺序、engineGetAttributes 默认空集合、engineProbe 默认 false、engineDeleteEntry、load/store 空实现兼容
- 异常：byte[] setKeyEntry 按本 SPI 抛 KeyStoreException（契约允许）

断言入口：`KeyStoreSpiTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
