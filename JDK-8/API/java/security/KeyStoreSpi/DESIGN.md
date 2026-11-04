# java.security.KeyStoreSpi 测试设计（JDK-8）

## 1. API 功能
- 抽象引擎类，`KeyStore` 外观的 SPI 后端；public 无参构造；JDK-8 有 16 个抽象引擎方法。
- 5 个默认方法（本套重点）：`engineStore(LoadStoreParameter)`、`engineLoad(LoadStoreParameter)`、`engineGetEntry`、`engineSetEntry`、`engineEntryInstanceOf`。

## 2. Java 用户如何使用
密钥库 provider 继承 KeyStoreSpi 实现 16 个抽象方法，默认方法提供 LoadStoreParameter/Entry 适配；经 `KeyStore.getInstance("JKS")` 装配。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/security/KeyStoreSpi.java`
- JDK 测试 `/data/yansendao/git/jdk8u-dev/jdk/test/java/security/KeyStore/`
- JKS 实现 `sun.security.provider.JavaKeyStore`（KeyProtector PKCS#8 封装、别名小写化）

## 4. 测试用例设计 + 自动化实现
- 结构：16-abstract 精确计数；5 个默认方法非抽象、engineStore(param) 为 public。
- DefaultsSpi 记录子类（不覆写 5 个默认方法）逐项钉死 JDK 默认行为：
  - `engineStore(param)` → UOE（含 null param）。
  - `engineLoad(null param)` → 转调 `engineLoad(null,null)`；**`engineLoad(带密码 param)` → JDK-8 UOE（JDK-9+ 转调 engineLoad(null,password)，版本差异）**。
  - `engineGetEntry` 矩阵：absent→null；key 条目+PasswordProtection（链 null）→ NPE "invalid null input"；+CallbackHandlerProtection → NPE；+null 保护 → `UnrecoverableKeyException`；cert 条目+null → TrustedCertificateEntry（同证书）。
  - `engineSetEntry` 适配：SecretKeyEntry→engineSetKeyEntry(密码透传)；TrustedCertificateEntry(+null)→engineSetCertificateEntry；PrivateKeyEntry→engineSetKeyEntry 带链（**PrivateKeyEntry 构造校验私钥算法==证书公钥算法，否则 IllegalArgumentException**）。
  - `engineEntryInstanceOf`：key 条目+null 链 → 非 PrivateKeyEntry、是 SecretKeyEntry；cert 条目→TCE；absent→false。
- 外观闭环（真实 JKS SPI，内嵌 keytool DSA 证书 + 运行时生成 DSA-1024 密钥）：load(null)+setKeyEntry+getKey equals（PKCS#8）、**错密码 → `UnrecoverableKeyException`（不是 null）**、链/creationDate/cert 条目同一实例、**JKS 别名大小写不敏感（内部小写）**、store→load 字节级往返保两项、**错 store 密码 load → `IOException`**、deleteEntry 生效、未 load 的 KeyStore 一切操作 → `KeyStoreException`。

断言入口：`KeyStoreSpiTest.testContractDeep()`（21→60 断言）。
