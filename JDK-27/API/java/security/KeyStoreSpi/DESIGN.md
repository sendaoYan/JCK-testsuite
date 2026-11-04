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

## 5. 契约深化（testContractDeep，本轮新增）
- 16-abstract 精确计数；DefaultsSpi 不覆写 5 个默认方法，钉死 JDK 默认行为：
  - engineStore(param) → UOE（含 null）。
  - engineLoad(null param) → 转调 engineLoad(null,null)；JDK-9+ 带密码 param → 转调 engineLoad(null,password)（JDK-8 为 UOE，版本差异）。
  - engineGetEntry 矩阵：absent→null；key+PasswordProtection（链 null）→NPE "invalid null input"；key+CBH →NPE "handler must not be null"；key+null 保护→UnrecoverableKeyException；cert+null → TrustedCertificateEntry。
  - engineSetEntry 三种适配（SKE→setKeyEntry 密码透传、TCE→setCertificateEntry、PKE→setKeyEntry 带链；PrivateKeyEntry 构造校验私钥算法==证书公钥算法）。
  - engineEntryInstanceOf：key+null 链→SKE 非 PKE；cert→TCE；absent→false。
  - JDK-9+ 默认：engineGetCreationInstant=EPOCH 且 null 安全、engineGetAttributes 空 Set、engineProbe(null)→NPE/未知流→false。
- JKS 外观闭环（RSA 夹具）：getKey equals（PKCS#8）、错密码→UnrecoverableKeyException、store/load 往返、错 store 密码→IOException、别名大小写不敏感、未 load→KeyStoreException。
