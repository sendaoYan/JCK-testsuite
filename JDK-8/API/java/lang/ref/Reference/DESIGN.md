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

## 契约深化（2026-11，批次 lcL）

### 1. API 功能（本轮新增维度）
- get() 同一实例稳定性：强可达期间 get() 每次返回同一 referent 实例（含 System.gc() 后，规范保证）。
- enqueue() 会**清除 referent**（jdk8u372 与 jdk-27.0.1 双侧实测一致：手动 enqueue 后 get()==null）。
- 已 clear 的已注册引用 enqueue() 返回 true；被 poll 取走后 enqueue() 返回 false（一次性语义）。
- null referent 引用可正常入队（enqueue true、poll 返回自身）。
- Object 契约：equals 为身份相等（同 referent 两个引用不等）、hashCode 稳定、toString 形如 `java.lang.ref.WeakReference@<hex>`、Reference 家族**非 Serializable**、Reference 抽象、三个子类均为其直接子类。
- 队列独立性：两个队列互不串扰。

### 2. 使用方式
- 断言矩阵驱动：6 种 referent 样本（String/Integer/long[]/Object/StringBuilder/Long）× 身份/稳定/GC 存活/clear 独立性。

### 3. 参考来源
- Oracle javadoc java.lang.ref.Reference（Java SE 8 / 27）；双侧 jdk8u372-b07、jdk-27.0.1.0.2+1 实测（/tmp probe）。
- JDK 16+ `Reference.enqueue` javadoc（"Clears this reference object and adds it to the queue"）——8 侧行为相同（实测）。

### 4. 测试用例设计 + 易错规范细节
- testGetIdentityStability（6 样本×7 断言）、testEnqueueDequeueStateMachine、testReferenceKindsOnSameObject（weak/soft/phantom 同对象三视图）、testReferenceObjectContract、testQueueIndependence、testIsEnqueuedDeprecationSurface（8 侧：无 @Deprecated；27 侧：@Deprecated(since="16", forRemoval=false)——版本差异锁定）。
- 易错点：① 不用手动 enqueue 的 FIFO 顺序（实测非 FIFO，只做集合语义）；② enqueue() 清 referent 双侧一致，勿在 enqueue 后断言 get()!=null；③ Object.clone 在 27 侧因 java.base 未开放无法反射探测（记入差异，跳过）。
