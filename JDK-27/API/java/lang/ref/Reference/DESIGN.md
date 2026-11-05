# java.lang.ref.Reference 测试设计案（JDK-27）

> 覆盖目标：完整契约。get/clear/enqueue/isEnqueued 状态机、无队列 enqueue 返回 false、clear 后 enqueue false、
> 有队列手动 enqueue true + 二次 enqueue false、ReferenceQueue poll/remove(1)/remove(-1)→IAE、WeakReference
> 有界 GC 回收、SoftReference 引用保留、PhantomReference get() 恒 null + 队列注册。

## 1. API 功能

- `get()`/`clear()`/`enqueue()`/`isEnqueued()`；抽象基类，子类 Weak/Soft/Phantom。
- 配合 ReferenceQueue 观察回收/入队。

## 2. 用户如何使用

- `new WeakReference<>(obj, queue)` 注册弱引用；GC 后 `queue.poll()` 取回被回收的引用。
- `clear()` 主动切断引用；`enqueue()` 手动入队。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/ref/Reference.html）。
- OpenJDK 8 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/ref/Reference.java`。
- JCK runtime-8b `tests/api/java_lang/ref/Reference/`。

## 4. 测试用例设计（映射到 ReferenceTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | get/clear/enqueue 状态机 | testGetClearEnqueue |
| P0 | ReferenceQueue remove(1)/remove(-1)→IAE | testReferenceQueueRemove |
| P1 | WeakReference 有界 GC 回收 | testWeakReferenceGC |
| P1 | SoftReference 引用保留 | testSoftReference |
| P1 | PhantomReference get() 恒 null + 入队 | testPhantomReference |

## 易错规范细节（已锁入断言）

- 无队列 `enqueue()` 返回 false；clear 后 `get()` 为 null 且 enqueue false。
- 有队列手动 `enqueue()` 首次 true，再次 false；poll 后 isEnqueued false。
- `remove(-1)` → IllegalArgumentException（JDK-8）。

## JDK-27 差异（已锁入断言）

- 新增 `refersTo(T)`（live referent 真、其他/null 假、clear 后假）与 `reachabilityFence(Object)`（testRefersTo）。

## 契约深化（2026-11，批次 lcL）

### 1. API 功能（本轮新增维度）
- 同 JDK-8 侧全部维度（身份稳定矩阵、enqueue 清 referent、clear 后 enqueue true、poll 后 enqueue false、null referent 入队、Object 契约、队列独立性）。
- JDK-27 独有：`refersTo(T)`（final，boolean）：live referent true / 他对象 false / **clear 后 refersTo(null)==true**（referent 已是 null——易错点）；`reachabilityFence(Object)`（含 null 实参不抛）；`isEnqueued` 带 `@Deprecated(since="16", forRemoval=false)`。

### 2. 使用方式
- refersTo 是 phantom referent 唯一可观测手段（get 恒 null）；reachabilityFence 用于保证副作用可见的排序。

### 3. 参考来源
- Oracle javadoc（Java SE 27）java.lang.ref.Reference：refersTo "true if and only if the reference object is being *to the given object"（含 null 语义）；JDK 16 变更记录（isEnqueued 弃用）。
- jdk-27.0.1.0.2+1 javap：`public final boolean refersTo(T)`、`public static void reachabilityFence(java.lang.Object)`。

### 4. 测试用例设计 + 易错规范细节
- testRefersToDeep：phantom live refersTo true（与 get()==null 并存的核心对照）、clear 后 refersTo(null)==true、refersTo 是 final 方法（反射断言）。
- testIsEnqueuedDeprecationSurface：27 侧 since="16" 且 forRemoval=false（8 侧无注解，同文件族双版本差异）。
- 易错点：refersTo(null) 并非恒 false——referent 为 null 时返回 true；勿断言手动入队顺序。
