# javax.crypto.ExemptionMechanism 设计案

## 1. API 功能
豁免机制引擎（public 具体类，无公共构造器，保护构造器 `protected ExemptionMechanism(ExemptionMechanismSpi, Provider, String)`）：
- 工厂：`getInstance(String)` / `getInstance(String, String)` / `getInstance(String, Provider)`（static final）。
- 属性：`getName()`、`getProvider()`。
- 初始化：`init(Key)`、`init(Key, AlgorithmParameterSpec)`、`init(Key, AlgorithmParameters)`。
- 状态/生成：`isCryptoAllowed(Key)`（blob 成功生成且 key 与 init 时相同的才 true）、`getOutputSize(int)`、`genExemptionBlob()` / `genExemptionBlob(byte[])` / `genExemptionBlob(byte[], int)`。

## 2. Java 用户如何使用
运行时豁免（key recovery/escrow）编程接口：`getInstance("KeyRecovery")` → `init(key)` → `genExemptionBlob()` 生成交给托管方的 blob，之后用 `isCryptoAllowed(key)` 判定该密钥已可使用。Temurin JDK-27 **不含任何实现**（实测 "KeyRecovery"/"KeyEscrow"/"KeyWeakening" 及未知名均 NoSuchAlgorithmException），因此实例级行为只能经 protected 构造器 + 自定义 Spi 驱动包装类状态机：未 init 时 getOutputSize/genExemptionBlob 抛 IllegalStateException；init 成功后 engineGetOutputSize 生效（负长度 IllegalArgumentException）；blob 生成成功后 done 标志使 isCryptoAllowed(同 key)=true、异 key/false/null=false；再次 init 清 done；短缓冲透传 ShortBufferException；engine 的 ExemptionMechanismException 透传且不置 done。

## 3. 开源代码实际用法调研
web 搜索在当前环境不可用，采用 JDK 源码：
- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/ExemptionMechanism.java` L70-89（字段 done/initialized/keyStored）、L272-283（isCryptoAllowed：`done && key != null && keyStored.equals(key)`）、L287-314（getOutputSize：未初始化 IllegalStateException("ExemptionMechanism not initialized")、负长度 IllegalArgumentException、否则委托 engine）、L334-353（init：置 done=false/initialized=false 后委托 engine，成功后 initialized=true）、L412-419（genExemptionBlob：未初始化 IllegalStateException，成功后 done=true）。本设计的全部状态机断言逐条对应这些实现。
- JDK 自带测试目录无独立 ExemptionMechanism 用例（`~/git/jdk25u-dev/test/jdk` 检索无命中），因无实现可用——与本项目实测一致，已在 DESIGN 与用例中注明。

## 4. 测试用例设计 + 自动化实现
`ExemptionMechanismTest.java`：
- testWhiteBox：public 具体类、父类 Object、无公共构造器（保留旧断言）、protected (Spi,Provider,String) 构造器、6 个实例方法均为 final、getInstance 为 static final。
- testBlackBox：
  - `factoryErrors`：Temurin 无实现——"NoSuchMechanism"（保留旧断言）、"KeyRecovery"、"KeyEscrow"、"KeyWeakening"×（默认/具名 provider/Provider 对象）共 5 种 `NoSuchAlgorithmException`；未知 provider 名 `NoSuchProviderException`；getInstance(null) NPE。
  - `wrapperStateMachine`：MySpi+MyMech（protected 构造器）——getName/getProvider；未 init 时 getOutputSize 与 genExemptionBlob 三个重载均 IllegalStateException；isCryptoAllowed 各阶段（false→init 后仍 false→生成后 true→异 key false→null false→再 init 又 false→再生成又 true）；getOutputSize 委托与负长度 IllegalArgumentException；init 三重载分别记录 spec/params 身份；blob 三个重载内容/偏移/返回长度；短缓冲 ShortBufferException；engine 失败时 ExemptionMechanismException 透传且 done 保持 false。
