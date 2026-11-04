# java.security.AlgorithmParametersSpi 测试设计（JDK-8）

## 1. API 功能
- 抽象引擎类，`AlgorithmParameters` 外观的 SPI 后端；public 无参构造。
- 恰好 7 个抽象 protected 方法（4 个声明）：`engineInit(spec)`、`engineInit(byte[])`、`engineInit(byte[],String)`、`engineGetParameterSpec(Class)`、`engineGetEncoded()`、`engineGetEncoded(String)`、`engineToString`。
- 异常声明：`engineInit(spec)` → `InvalidParameterSpecException`；`engineInit(byte[][,String])` → `IOException`。

## 2. Java 用户如何使用
provider 继承 AlgorithmParametersSpi 实现参数的 spec/编码互转，注册后由 `AlgorithmParameters.getInstance("DSA")` 装配。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/security/AlgorithmParametersSpi.java`
- JDK 测试 `/data/yansendao/git/jdk8u-dev/jdk/test/java/security/AlgorithmParameters/`
- DSA 实现 `sun.security.provider.DSAParameters`

## 4. 测试用例设计 + 自动化实现
- 结构：exact-7-abstract、异常声明精确（IPSE/IOException）。
- 透明记录子类：三个 `engineInit` 重载、`engineGetParameterSpec`（记录 Class）、两个 `engineGetEncoded` 重载（互为独立方法，计数验证）、`engineToString` 全部精确透传。
- 外观闭环（真实 DSA SPI）：init(DSAParameterSpec) → getEncoded() → 新实例 init(bytes) → getParameterSpec p/q/g equals；`toString()` 含 "p:"/"q:"/"g:" 段。
- 外观状态机：未 init 时 `getEncoded()` → `IOException`、`getParameterSpec` → `InvalidParameterSpecException`、**`toString()` 返回 null 引用（打印为 "null"）**；`init(null spec)` → IPSE。

断言入口：`AlgorithmParametersSpiTest.testContractDeep()`（7→29 断言）。
