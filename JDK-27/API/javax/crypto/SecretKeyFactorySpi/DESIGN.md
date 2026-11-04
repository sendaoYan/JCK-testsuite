# javax.crypto.SecretKeyFactorySpi 设计案

## 1. API 功能

密钥工厂 SPI，`public abstract class`，恰好 3 个声明方法：

- **构造器**：public 无参。
- **protected abstract**：`engineGenerateSecret(KeySpec)`（声明 `InvalidKeySpecException`，返回 `SecretKey`）、`engineGetKeySpec(SecretKey, Class<?>)`（声明 `InvalidKeySpecException`，返回 `KeySpec`）、`engineTranslateKey(SecretKey)`（声明 `InvalidKeyException`，返回 `SecretKey`）。

## 2. Java 用户如何使用

Provider 实现者继承并实现 3 个方法，注册为 `SecretKeyFactory.X` 服务。另一路径是 protected `SecretKeyFactory(spi, provider, algorithm)` 构造器直接组装：`class MySKF extends SecretKeyFactory { MySKF(spi, p, "X") { super(spi, p, "X"); } }`，随后 `generateSecret/getKeySpec/translateKey` 全部分派到自己的 SPI。受保护方法可在子类内以 public 适配器暴露，供包外驱动。

## 3. 开源代码实际用法调研

（WebSearch 对该 SPI 无直接命中，以下为 JDK 源码/自带测试，标注路径。）

- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/SecretKeyFactorySpi.java` 全文即契约：3 个 protected abstract 方法 + public 无参构造器，异常声明即本测试白盒的 declared-exception 断言。
- `~/git/jdk25u-dev/src/java.base/share/classes/com/sun/crypto/provider/DESedeKeyFactory.java`：SunJCE 真实子类——`engineGenerateSecret(DESedeKeySpec)` 造 `DESedeKey`、`engineGetKeySpec` 反向回 DESedeKeySpec、`engineTranslateKey` 校验算法后重包，是 `FixedSpi` 三方法行为的原型。
- `~/git/jdk25u-dev/src/java.base/share/classes/com/sun/crypto/provider/PBKDF2KeyFactory.java`：`engineGenerateSecret(PBEKeySpec)` 按 keyLength 派生、`engineGetKeySpec` 回 `PBEKeySpec`（仅当 key 是本 provider 的 PBKDF2KeyImpl，否则 `InvalidKeySpecException`——黑盒"getKeySpec(非 PBE key) 异常"即此路径）。
- `~/git/jdk25u-dev/test/jdk/javax/crypto/SecretKeyFactory/PBKDF2TranslateTest.java` 与 `evilprov/`：用假 provider/evil provider 驱动 `engineTranslateKey` 分派链，验证包装器正确调用 SPI。
- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/SecretKeyFactory.java`（protected 构造器与三个 final 委托方法）。

## 4. 测试用例设计 + 自动化实现

`SecretKeyFactorySpiTest.java`，`testWhiteBox()`（结构，14 check）：public abstract、extends Object、无接口、public 无参构造器、3 个方法均 protected abstract、精确签名/返回类型、声明方法总数恰为 3、`engineGenerateSecret` 声明 `InvalidKeySpecException`、`engineTranslateKey` 声明 `InvalidKeyException`。

`testBlackBox()`（行为，17 check，SPI 类按 ≥5 要求）场景（`FixedSpi` 最小子类 + 计数）：

1. `engineGenerateSecret(SecretKeySpec)` 收到 spec 引用，返回同字节新 key（算法换为子类值）。
2. `engineGetKeySpec(key, SecretKeySpec.class)` 收到 key 与目标类，返回携带相同字节的 spec。
3. `engineTranslateKey(key)` 原样返回（恒等翻译），计数递增。
4. 异常契约由实现者掌控：不支持的 spec 类型 → `InvalidKeySpecException`；不支持的目标 spec 类 → `InvalidKeySpecException`；外来算法 key → `InvalidKeyException`（三条各验证）。
5. 经 protected `SecretKeyFactory(spi, provider, algorithm)` 组装：`getAlgorithm`/`getProvider` 透传；`generateSecret`/`getKeySpec`/`translateKey` 分派到对应 engine 方法并返回 SPI 产物；`translateKey` 与 `generateSecret` 的 SPI 异常经包装器原样传播。
