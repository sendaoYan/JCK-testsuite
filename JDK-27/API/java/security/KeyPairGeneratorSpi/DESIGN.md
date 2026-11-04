# java.security.KeyPairGeneratorSpi

## 1. API 功能
密钥对生成器的服务提供者接口。实现者需提供 `initialize(int, SecureRandom)` 和 `generateKeyPair()`。`initialize(AlgorithmParameterSpec, SecureRandom)` 是具体方法，若提供者未覆写则默认抛 `UnsupportedOperationException`。

## 2. Java 用户如何使用
用户通过 `KeyPairGenerator.getInstance("RSA")` 获取封装实例，`initialize(...)` 与 `generateKeyPair()` 最终委托给 SPI 的引擎方法。提供者子类实现算法细节，返回 `KeyPair`（内含 `PublicKey`/`PrivateKey`）。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/KeyPairGeneratorSpi.java`（JDK 源）：
  - `public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException { throw new UnsupportedOperationException(); }`
- `test/jdk/java/security/KeyPairGenerator/Failover.java`：
  - `public static class KeyPairGeneratorPass extends KeyPairGeneratorSpi { public void initialize(int keysize, SecureRandom random) {...} public KeyPair generateKeyPair() {...} }`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言为 public abstract class、超类 `Object`、含 `generateKeyPair`/`initialize` 方法。
- `testBlackBox()`：最小子类 `MyKpgSpi` 用 `DummyPublicKey`/`DummyPrivateKey`（algorithm/format 为 "Dummy"，`getEncoded()` 为 `{1,2,3}`）构造 `KeyPair`；断言 `generateKeyPair()` 返回非空且公私钥非空、算法/编码正确，并断言未覆写的 `initialize(AlgorithmParameterSpec, SecureRandom)` 抛 `UnsupportedOperationException`。

## 5. 契约深化（testContractDeep，本轮新增）
- 引擎方法为 public 修饰（本家族唯一）；initialize(spec,random) 为非抽象默认、声明 IAPE。
- 真实 DSA 外观闭环：generateKeyPair() 公/私钥格式 X.509/PKCS#8、两次生成不同、实例关联 KeyPairGeneratorSpi。
- initialize 异常矩阵：(-1)/(64) → InvalidParameterException、1-bit DSA 参数 → IPE（强度先查）、RSA spec 喂 DSA → InvalidAlgorithmParameterException。
