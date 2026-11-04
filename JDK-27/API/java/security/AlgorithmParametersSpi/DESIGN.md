# java.security.AlgorithmParametersSpi

## 1. API 功能
算法参数的服务提供者接口。实现者需提供七个抽象引擎方法：三个 `engineInit`（分别接受 `AlgorithmParameterSpec`、`byte[]`、`byte[]+format`）、`engineGetParameterSpec(Class<T>)`、两个 `engineGetEncoded`（无参和带 format）、`engineToString()`，用于参数的初始化、编码与字符串化。

## 2. Java 用户如何使用
用户通过 `AlgorithmParameters.getInstance("EC")` 获取封装实例，调用 `init`/`getParameterSpec`/`getEncoded`/`toString`，由 `AlgorithmParameters` 转发给 SPI。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/AlgorithmParametersSpi.java`（JDK 源）：声明七个 abstract 引擎方法。
- `test/jdk/javax/crypto/KEM/RSA_KEM.java`：
  - `public static class AlgorithmParametersImpl extends AlgorithmParametersSpi { protected void engineInit(AlgorithmParameterSpec paramSpec) {...} protected void engineInit(byte[] params) throws IOException {...} protected byte[] engineGetEncoded() {...} protected String engineToString() {...} }`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言为 public abstract class、超类 `Object`。
- `testBlackBox()`：最小子类 `MyApSpi`（`engineGetEncoded` 返回 `{7}`、`engineToString` 返回 "MyApSpi"）断言三个 `engineInit` 正常执行、两个 `engineGetEncoded` 返回 `{7}`、`engineToString()=="MyApSpi"`。

## 5. 契约深化（testContractDeep，本轮新增）
- exact-7-abstract；异常声明精确（engineInit(spec)→IPSE、engineInit(byte[][,String])→IOException）。
- 记录子类 7 方法透传；两个 engineGetEncoded 重载互为独立方法（计数验证）。
- 真实 DSA 外观闭环：init(spec)→getEncoded()→新实例 init(bytes)→p/q/g equals；toString 含 "p:"/"q:"/"g:"。
- 未 init 状态机：getEncoded→IOException、getParameterSpec→InvalidParameterSpecException、toString 返回 null 引用；init(null spec)→IPSE。
