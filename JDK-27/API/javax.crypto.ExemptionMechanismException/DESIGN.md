# javax.crypto.ExemptionMechanismException 设计案

## 1. API 功能
豁免机制异常（public，直接父类 `java.security.GeneralSecurityException`，即受检异常）。JDK-27 javap 实测仅两个公共构造器：
- `ExemptionMechanismException()`
- `ExemptionMechanismException(String message)`
本版本**没有** (String, Throwable) 构造器；cause 只能经 Throwable 的 `initCause` 补充。

## 2. Java 用户如何使用
作为 `ExemptionMechanism.init/genExemptionBlob/isCryptoAllowed` 声明的受检异常，典型用法是在实现豁免机制的代码里 `throw new ExemptionMechanismException("blob unusable")`，调用方按 `GeneralSecurityException` 分支统一处理；无参版本表示原因不明的失败。message 经 getMessage/getLocalizedMessage/toString 暴露；异常对象可序列化、可挂 suppressed、cause 可后续注入。

## 3. 开源代码实际用法调研
web 搜索在当前环境不可用，采用 JDK 源码：
- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/ExemptionMechanismException.java`：仅 `(String)` 与 `()` 两个构造器（与 JDK-27 javap 一致）。
- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/ExemptionMechanism.java` L248（isCryptoAllowed 声明 throws）、L334/L346/L358（init 三重载声明 throws）、L412-419（genExemptionBlob 声明 throws）——该异常贯穿豁免机制全部公共入口，ExemptionMechanismTest 中 Spi 抛出的实例被包装类原样透传，即真实用法样例。

## 4. 测试用例设计 + 自动化实现
`ExemptionMechanismExceptionTest.java`：
- testWhiteBox：public 具体非 final 类、直接父类 GeneralSecurityException、恰有 2 个公共构造器（无参 + String）、无 (String,Throwable) 构造器。
- testBlackBox：保留旧断言（"msg" 构造器 message、instanceof GeneralSecurityException×2、无参 message null）并补充：无参 cause null 且栈非空；getMessage/getLocalizedMessage/toString（含类名+消息）；initCause 注入与读取；经 `thrower` 方法的 throw/catch 链路（按本类型与按 GeneralSecurityException 各捕获一次）；addSuppressed/getSuppressed；对象序列化往返后类型与 message 保持。
