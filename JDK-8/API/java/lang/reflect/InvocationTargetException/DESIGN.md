# java.lang.reflect.InvocationTargetException — 测试设计案（JDK-8）

## 1. API 功能

- `InvocationTargetException extends ReflectiveOperationException`（受检异常）：
  - 公有构造器 `(Throwable target)`、`(Throwable target, String detailMessage)`；protected 无参构造（只能经子类调用，target 永远无法后补）。
  - `getTargetException()` 与 `getCause()` 返回同一引用；target 为 null 时两者均 null 且 `initCause` 被拒。
  - 经 Throwable 继承 Serializable。

## 2. 用户如何使用

- `Method.invoke`/`Constructor.newInstance` 被调方抛出的异常一律包装成 ITE；捕获后 `getCause()` 解包根因再处理/重抛（Spring/JUnit 反射工具的惯用写法）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/reflect/InvocationTargetException.html：构造器与 target 语义）。
- OpenJDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/InvocationTargetException.java`。
- OpenJDK 8 回归 `~/git/jdk8u-dev/jdk/test/java/lang/reflect/`（Generics/exceptionCauseTest 同构场景）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/reflect/InvocationTargetException/`。

## 4. 测试用例设计（映射到 InvocationTargetExceptionTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 基本构造器/cause/target（保留原有） | testInvocationTargetException |
| P0 | 层次普查：ReflectiveOperationException/Exception/Serializable、2 个公有构造器 | testHierarchy |
| P0 | 显式 null target：getCause/getTargetException/getMessage 均 null、initCause → ISE "Can't overwrite cause" | testNullTarget |
| P0 | protected 无参构造（子类）：target 不可后补（initCause 被拒） | testProtectedNoArg |
| P0 | 真实触发：Method.invoke 包装运行时/受检异常，cause 类型+消息+target==cause 同一引用 | testRealInvokeWrap |
| P1 | 构造器状态：消息保留、引用保留、bare 无消息、toString、Error target、re-init 拒绝 | testConstructorState |
| P1 | addSuppressed + printStackTrace（UTF-8 流）渲染 wrapper/cause/suppressed | testSuppressedAndPrinting |
| P1 | 序列化 round-trip：消息/cause 类型+消息/target==cause 链/suppressed 保留（cause 是副本） | testSerialization |
| P2 | 受检异常持有无需声明、toString 以 FQCN 开头 | testCheckedHolderSemantics |

## 易错规范细节（已锁入断言）

- `(Throwable)` 单参构造内部 `super((Throwable)null)` 把 cause 钉死为 null：之后 `initCause` 一律 `IllegalStateException`（消息含 "Can't overwrite cause"），8 与 27 一致。
- getTargetException()/getCause() 恒等（同一对象），序列化 round-trip 后该恒等关系仍成立（但对象本身是反序列化副本）。
- javap 实测：protected 无参 + 2 个公有构造器，两侧一致。
