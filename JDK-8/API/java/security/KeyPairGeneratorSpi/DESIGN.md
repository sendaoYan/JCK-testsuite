# java.security.KeyPairGeneratorSpi 测试设计（JDK-8）

## 1. API 功能
- 抽象引擎类，`KeyPairGenerator` 外观的 SPI 后端；public 无参构造。
- 唯一抽象方法 `engineGenerateKeyPair()`；其余 `engineInitialize(int,SecureRandom)`、`engineInitialize(AlgorithmParameterSpec,SecureRandom)` 为**非抽象默认（空实现）**——与多数 SPI 不同。
- 引擎方法访问修饰为 public（非 protected），声明 `InvalidAlgorithmParameterException`。

## 2. Java 用户如何使用
provider 继承 KeyPairGeneratorSpi，注册后由 `KeyPairGenerator.getInstance("DSA")` 装配生成密钥对。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/security/KeyPairGeneratorSpi.java`
- JDK 测试 `/data/yansendao/git/jdk8u-dev/jdk/test/java/security/KeyPairGenerator/`
- DSA 实现 `sun.security.provider.DSAKeyPairGenerator`

## 4. 测试用例设计 + 自动化实现
- 结构：**引擎方法为 public（唯一 public 修饰的 SPI 家族）**、`initialize(spec,random)` 默认实现存在且声明 IAPE。
- 外观闭环（真实 DSA SPI）：`getInstance("DSA").generateKeyPair()` → KeyPair，私钥 instanceof KeyPairGeneratorSpi 关联断言、公/私钥格式 X.509/PKCS#8、两次生成不同。
- 外观异常矩阵：`initialize(-1)`/`initialize(64)` → `InvalidParameterException`；1-bit DSA 参数 → IPE（强度先于参数类型检查）；RSA spec 喂 DSA → `InvalidAlgorithmParameterException`。
- **易错点：`KeyPairGeneratorSpi.initialize(spec,random)` 抽象类默认是空实现，但 `KeyPairGenerator` 外观的 spec 初始化在 DSA 之外抛 UOE/IAPE 的行为由具体实现决定，不能按 javadoc 推断为 IAPE——本套以实测为准**。

断言入口：`KeyPairGeneratorSpiTest.testContractDeep()`（5→24 断言）。
