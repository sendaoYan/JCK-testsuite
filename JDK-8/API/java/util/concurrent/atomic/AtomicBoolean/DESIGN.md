# java.util.concurrent.atomic.AtomicBoolean 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`AtomicBoolean()`（初值 false）、`AtomicBoolean(boolean initialValue)`。
- **读/写**：`get()`、`set(boolean)`、`lazySet(boolean)`（最终可见写）。
- **原子更新**：`compareAndSet(expect, update)`（按位语义，两分支返回值精确）、
  `weakCompareAndSet(expect, update)`（JDK-8 未废弃；JDK-9 起 @Deprecated）、`getAndSet(newValue)`（返回旧布尔）。
- **toString**：返回当前值字符串（"true"/"false"）。
- **继承/契约**：`extends Object implements Serializable`（不是 Number）。

## 2. Java 用户如何使用

- 状态标志位：CAS 抢占一次性开关（`if (flag.compareAndSet(false, true))` 恰一线程成功）。
- 原子开关/配置覆写：`getAndSet` 无条件翻转并取旧值。

## 3. 开源/源码实际用法调研

- JDK 源码 `java.util.concurrent` 大量内部用 AtomicBoolean 表达"已初始化/已关闭"标志。
- OpenJDK 回归 `test/jdk/java/util/concurrent/atomic/`：CAS 两分支 + lazySet 可见性。
- JCK runtime-8b `api/java_util_concurrent_atomic/AtomicBoolean`：构造器/访问器/序列化。

## 4. 测试用例设计（映射到 AtomicBooleanTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 两个构造器初值 | testConstructors |
| P0 | CAS 成功/失败两分支（值变/不变 + 返回值精确） | testAtomicBoolean + testCasMatrix |
| P0 | getAndSet 双向返回旧布尔 | testCasMatrix |
| P0 | lazySet 同线程立即 get 可见 | testLazySetAndWeak |
| P0 | weakCompareAndSet 两分支（失败确定、成功有界重试） | testLazySetAndWeak |
| P1 | toString 精确值 "true"/"false" | testToStringAndSerializable |
| P1 | 序列化 round-trip 保值且复活可操作 | testToStringAndSerializable |
| P1 | JDK-9+ 访问模式族（getPlain/setPlain/getOpaque/setOpaque/getAcquire/setRelease/compareAndExchange×3/weakCAS×4）缺席反射断言 | testAbsentJdk9Methods |
| P1 | weakCompareAndSet 未标 @Deprecated（JDK-8 现状） | testAbsentJdk9Methods |

## 关键规范细节（易错点）

- JDK-8 的 AtomicBoolean **没有** getPlain/setOpaque/compareAndExchange 等 JDK-9 VarHandle 访问模式族——本测试用反射锁定缺席（版本差异记录点）。
- weakCompareAndSet 成功分支允许虚假失败（spurious failure），测试用 ≤100k 次有界重试后断言成功，不得死循环。
- 序列化是标准 Java 序列化（值字段持久化），round-trip 后仍可 CAS。

## 断言规模

PASS (37 assertions)。
