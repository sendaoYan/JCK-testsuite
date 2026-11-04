# java.lang.reflect.UndeclaredThrowableException — 测试设计案（JDK-8）

## 1. API 功能

- `UndeclaredThrowableException extends RuntimeException`（非受检）：
  - 构造器仅 `(Throwable)`、`(Throwable, String)` —— javap 实测没有无参构造器，cause 由构造器恒被初始化（不存在"未初始化"路径）。
  - `getUndeclaredThrowable()` 与 `getCause()` 同一引用。
- Proxy 机制专用：handler 抛出的、接口方法未声明的受检异常被包装为 UTE（消息为 null，cause 即原异常对象）。

## 2. 用户如何使用

- 调用动态代理后捕获 UTE 并 `getCause()` 取真实根因；构造侧无需 throws 声明（RuntimeException 分支）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/reflect/UndeclaredThrowableException.html）。
- OpenJDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/UndeclaredThrowableException.java` 与 `Proxy.java`（包装点 ProxyGenerator）。
- OpenJDK 8 回归 `~/git/jdk8u-dev/jdk/test/java/lang/reflect/Proxy/`。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/reflect/UndeclaredThrowableException/`。

## 4. 测试用例设计（映射到 UndeclaredThrowableExceptionTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 基本构造器/cause/undeclared（保留原有） | testUndeclaredThrowableException |
| P0 | 层次普查：RuntimeException 直系、Serializable、恰 2 个公有构造器 | testHierarchyAndCensus |
| P0 | 非受检语义：无 throws 声明抛出/持有受检 cause | testUncheckedSemantics |
| P0 | 真实 Proxy 触发：未声明受检 → UTE（cause 同一对象、消息 null）、重复分发各自新 UTE 同 cause | testRealProxyTrigger |
| P0 | 声明受检/运行时异常/Error 均原样透传（不包装） | testRealProxyTrigger |
| P1 | 构造器状态：消息/引用保留、bare toString 精确、initCause 被拒 | testConstructorState |
| P1 | addSuppressed + printStackTrace | testSuppressedAndPrinting |
| P1 | 序列化 round-trip：undeclared==cause 链保留 | testSerialization |

## 易错规范细节（已锁入断言）

- Proxy 生成的 UTE message 为 null（JDK-8 与 JDK-27 实测一致）；`getUndeclaredThrowable() == getCause() == handler 抛出的同一对象`。
- 两个构造器都要求 Throwable，因此 cause 恒被预设：`initCause` 必得 `IllegalStateException`（"Can't overwrite cause"）。
- bare（无消息）toString 精确等于 FQCN；cause 只出现在 printStackTrace，不出现在 toString。
- 声明在接口方法 throws 列表里的受检异常、RuntimeException、Error 三类都原样透传，绝不包装。
