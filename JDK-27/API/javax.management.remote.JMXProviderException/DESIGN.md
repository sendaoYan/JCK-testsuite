# JMXProviderException 测试设计

## 1. API 功能
`javax.management.remote.JMXProviderException`（继承 `java.io.IOException`）表示找不到或无法实例化 JMX 连接器提供者。

- 构造器：`()`、`(String message)`、`(String message, Throwable cause)`。
- 查询：`getCause()` 返回构造时传入的 `Throwable`（覆盖 `Throwable.getCause()`），其余构造下为 null。

## 2. Java 用户如何使用
```java
throw new JMXProviderException("no provider", new ClassNotFoundException("x"));
// 调用方 catch (JMXProviderException e) 后读 e.getMessage() / e.getCause()
```
典型组合链路：`JMXConnectorFactory`/`JMXConnectorServerFactory` 在 `resolvePkgs`/加载提供者失败时抛 `JMXProviderException`，上层按 `IOException` 或专用类型捕获并诊断 `getCause()`。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorFactory.java`：`resolvePkgs` 与 `getProvider` 在提供者解析失败时 `throw new JMXProviderException(...)`，是 JDK 内部直接使用点。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXProviderException.java`：`getCause()` 返回私有 `cause` 字段。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
黑盒部分（`JMXProviderExceptionTest.testBlackBox()`）：
- 正常路径：`(message, cause)` 保留 message 与同一 cause 实例、`instanceof IOException`。
- 组合/边界：仅 message 构造 cause 为 null；无参构造 message/cause 均 null；`addSuppressed` 等标准 `Throwable` 机制可用。
