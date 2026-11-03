# java.security.AlgorithmParameterGeneratorSpi

## 1. API 功能
算法参数生成器的服务提供者接口。实现者需提供三个抽象引擎方法：`engineInit(int, SecureRandom)`、`engineInit(AlgorithmParameterSpec, SecureRandom)`、`engineGenerateParameters()`，用于按密钥尺寸或参数规范生成新的 `AlgorithmParameters`。

## 2. Java 用户如何使用
用户通过 `AlgorithmParameterGenerator.getInstance("DiffieHellman")` 获取封装实例，调用 `init(...)` 与 `generateParameters()`，最终委托给 SPI。提供者子类实现具体参数生成。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/AlgorithmParameterGeneratorSpi.java`（JDK 源）：声明三个 abstract 引擎方法。
- `src/java.base/share/classes/com/sun/crypto/provider/DHParameterGenerator.java`（JDK 源）：
  - `public final class DHParameterGenerator extends AlgorithmParameterGeneratorSpi { protected void engineInit(int keysize, SecureRandom random) {...} protected void engineInit(AlgorithmParameterSpec genParamSpec, ...) {...} protected AlgorithmParameters engineGenerateParameters() {...} }`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言为 public abstract class、超类 `Object`。
- `testBlackBox()`：最小子类 `MyApgSpi` 用 `AlgorithmParameters.getInstance("DSA")` 作为生成结果；断言两个 `engineInit` 正常执行、`engineGenerateParameters()` 返回非空且算法名非空。
