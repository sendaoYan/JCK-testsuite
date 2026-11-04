# java.rmi.RMISecurityException 行为测试设计

## 1. API 功能
`RMISecurityException` 继承 `java.lang.SecurityException`，已废弃。
- 构造器：`RMISecurityException(String)` 与 `RMISecurityException(String, String)`；后者忽略第二个参数，等价于 `this(name)`。

## 2. Java 用户如何使用
- 历史代码 `catch (RMISecurityException e)`；现代代码应直接使用 `SecurityException`。第二个构造器参数为历史遗留、被忽略。

## 3. 开源代码实际用法调研
- 类定义 `src/java.rmi/share/classes/java/rmi/RMISecurityException.java`：构造器注释明确 `arg ignored`。
- 无 JDK `test/jdk` 直接用例（已废弃类）。

## 4. 测试用例设计 + 自动化实现
黑盒部分（RMISecurityExceptionTest.testBlackBox）：单参构造 message；双参构造第二参被忽略、message 仍为第一参；cause 为 null。
白盒部分（testWhiteBox）：继承 SecurityException、非抽象/非 final、是 RuntimeException、@Deprecated 注解、两个构造器形态。
