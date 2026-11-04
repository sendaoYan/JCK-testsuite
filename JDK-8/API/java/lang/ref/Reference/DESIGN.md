# java.lang.ref.Reference 测试设计案（JDK-8）

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
