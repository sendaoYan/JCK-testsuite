# javax.crypto.KeyGeneratorSpi 设计案

## 1. API 功能

对称密钥生成 SPI，`public abstract class`，恰好 4 个声明方法：

- **构造器**：public 无参。
- **protected abstract**：`engineInit(SecureRandom)`、`engineInit(AlgorithmParameterSpec, SecureRandom)`（声明 `InvalidAlgorithmParameterException`）、`engineInit(int, SecureRandom)`、`engineGenerateKey()`（返回 `SecretKey`）。

## 2. Java 用户如何使用

普通用户不直接用；两条专业路径：
(1) 自定义 Provider：子类实现 4 个抽象方法并注册 `KeyGenerator.Foo` 服务（`Provider.Service`/`putService`）。
(2) 直接组装：`class MyKG extends KeyGenerator { MyKG(spi, provider, "X") { super(spi, provider, "X"); } }`——`init`/`generateKey` 即分派到自己的 SPI。受保护方法可经子类内 public 适配器从包外驱动。

## 3. 开源代码实际用法调研

（WebSearch 对该 SPI 无直接命中，以下为 JDK 源码/自带测试，标注路径。）

- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/KeyGeneratorSpi.java` 全文 4 个方法即契约；`engineGenerateKey()` 无参，密钥尺寸由先前的 `engineInit` 决定——黑盒"init(256) 后生成 32 字节"即验证该顺序语义。
- `~/git/jdk25u-dev/src/java.base/share/classes/com/sun/crypto/provider/AESKeyGenerator.java`：SunJCE 真实子类——`engineInit(int, SecureRandom)` 存 keysize、`engineGenerateKey()` 按存下的 keysize 生成，是本测试 `FixedKeySpi` 的原型。
- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/KeyGenerator.java` 的 protected 构造器 + `generateKey()` 直接委托 `spi.engineGenerateKey()`——对应黑盒包装分派断言；`init(int)` 为调用方补非 null `SecureRandom` 的行为也在源码 `init(int)` → `init(int, new SecureRandom())` 可见。
- `~/git/jdk25u-dev/test/jdk/javax/crypto/KeyGenerator/TestGetInstance.java`：用假 Provider 驱动 KeyGenerator 工厂对 SPI 的查找路径。

## 4. 测试用例设计 + 自动化实现

`KeyGeneratorSpiTest.java`，`testWhiteBox()`（结构，12 check）：public abstract、extends Object、无接口、public 无参构造器、4 个方法均 protected abstract 且精确签名（参数类型 + 返回 `javax.crypto.SecretKey`）、声明方法总数恰为 4。

`testBlackBox()`（行为，18 check，SPI 类按 ≥5 要求）场景（`FixedKeySpi` 最小子类按 `0xF0+i` 确定性生成）：

1. `engineInit(SecureRandom)` 收到引用相同的随机源。
2. `engineInit(int, SecureRandom)` 记录 keysize=256。
3. `engineGenerateKey()` 按最近一次 init 的尺寸生成 32 字节、算法名取子类值；连续两次生成结果相同（我们的实现确定性）。
4. `engineInit(AlgorithmParameterSpec, SecureRandom)` 记录 spec；对不支持的 spec 抛其声明的 `InvalidAlgorithmParameterException`（且不计为接受的 init）。
5. 经 protected `KeyGenerator(KeyGeneratorSpi, Provider, String)` 组装：`getAlgorithm`/`getProvider` 透传构造参数；`init(256)` 到达 `engineInit(int, SR)` 且补非 null 随机源；`generateKey()` 到达 `engineGenerateKey()` 并原样返回 SPI 的 key；`init(SecureRandom)`/`init(spec, SR)` 分派正确；`init(坏 spec)` 的 `InvalidAlgorithmParameterException` 经包装器传播。
