# java.security.AlgorithmParameterGeneratorSpi 测试设计（JDK-8）

## 1. API 功能
- 抽象引擎类，`AlgorithmParameterGenerator` 外观的 SPI 后端；public 无参构造。
- 3 个抽象 protected 方法：`engineInit(int,SecureRandom)`、`engineInit(AlgorithmParameterSpec,SecureRandom)`（声明 IAPE）、`engineGenerateParameters()`。

## 2. Java 用户如何使用
provider 继承 AlgorithmParameterGeneratorSpi 生成算法参数（如 DSA p/q/g），注册后由 `AlgorithmParameterGenerator.getInstance("DSA")` 装配。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/security/AlgorithmParameterGeneratorSpi.java`
- JDK 测试 `/data/yansendao/git/jdk8u-dev/jdk/test/java/security/AlgorithmParameterGenerator/`
- DSA 实现 `sun.security.provider/DSAParameterGenerator`

## 4. 测试用例设计 + 自动化实现
- 结构：exact-3-abstract、`engineInit(spec,random)` 声明 IAPE、protected 修饰。
- 透明记录子类（包一层 public 访问器暴露 protected 引擎方法）：int/spec 两种 init 精确透传（记录 size/spec/random 同一性），generateParameters 返回预置对象并计数。
- 外观闭环（真实 DSA SPI）：`init(1024,null).generateParameters()` 得 DSA AlgorithmParameters，携带 ASN.1 编码；`getParameterSpec(DSAParameterSpec)` p=1024bit、q=160bit；重复 generateParameters 正常。
- 外观异常矩阵：`init(0,null)`/`init(-512,null)` → `InvalidParameterException`；`init((spec)null,null)` → `InvalidAlgorithmParameterException`。

断言入口：`AlgorithmParameterGeneratorSpiTest.testContractDeep()`（5→21 断言）。
