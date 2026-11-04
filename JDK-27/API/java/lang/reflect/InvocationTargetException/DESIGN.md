# java.lang.reflect.InvocationTargetException — 测试设计（JDK-27）

## 1. API 功能
`InvocationTargetException extends ReflectiveOperationException`（受检）：
- 构造器：`InvocationTargetException(Throwable target)`、`(Throwable, String detailMessage)`、protected 无参。
- `getTargetException()`/`getCause()`：被包装的目标异常（同一引用）。
- `getMessage/initCause/addSuppressed/printStackTrace` 等 `Throwable` 行为。

## 2. Java 用户如何使用
`Method.invoke/Constructor.newInstance` 被调方抛出的异常一律包成 ITE → catch 后 `getCause()` 解包处理（框架常 `throw e.getCause()` 透传根因）。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Generics/exceptionCauseTest.java`：invoke 包装后 cause 精确性断言。
- Spring `ReflectionUtils.handleReflectionException`/`invokeMethod`：catch ITE 后解包重抛（同构场景 1/2）。
- JUnit `ReflectionUtils.invokeMethod`：`throw ite.getTargetException()` 透传测试异常。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/InvocationTargetException.java`。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`InvocationTargetExceptionTest.testBlackBox`，固定类 `Boom`）：
1. 真实触发：`invoke` 包装运行时异常（cause 精确类型+消息、target==cause 同一引用）。
2. 受检异常同样包装（IOException 精确类型+消息）。
3. 构造器状态：带消息/裸构造（消息 null、cause 按引用保留、toString 含类型名）。
4. protected 无参构造（子类）：cause/target null、消息 null、`initCause` 被拒 `IllegalStateException`（JDK-27 实测）。
5. 已设 cause 再 `initCause` → `IllegalStateException`。
6. `addSuppressed` + `printStackTrace` 渲染含根因（固定 UTF-8 流）。
白盒保留原有构造器断言 + 继承层次。产出：`InvocationTargetExceptionTest.java`（22 断言通过）。

## 深化批次（2026-11-05）

新增方法（断言 27 → 41）：

| 测试点 | 方法 |
|---|---|
| 层次普查：Serializable、直系父类、恰 2 个公有构造器 | testHierarchySerializable |
| 显式 null target：getCause/getTargetException/getMessage 均 null、initCause 拒绝 | testNullTargetExplicit |
| 序列化 round-trip：消息/cause 类型+消息/target==cause 链/suppressed 保留 | testSerializationRoundTrip |
| Error 型 target、toString FQCN 前缀 | testErrorTargetAndToString |

实测：null target 的 initCause 拒绝消息含 "Can't overwrite cause"，8/27 措辞一致。
