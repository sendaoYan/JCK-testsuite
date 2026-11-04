# java.lang.ref.Reference — 设计方案

## 1. API 功能
引用对象的抽象基类（package-private 构造器，只能经子类 `SoftReference`/`WeakReference`/`PhantomReference`/`Cleaner` 内部使用）：
- 查询：`get()`（返回引用对象或 null）、`refersTo(T)`（final，身份比较，不排队）、`isEnqueued()`
- 操作：`clear()`（丢弃引用，不排队）、`enqueue()`（手动入队，已入队/未注册返回 false）
- 工具：静态 `reachabilityFence(Object)`（保证强可达）、`protected clone()`

## 2. Java 用户如何使用
用 WeakReference 包住大对象做缓存，配合 ReferenceQueue 做失效通知：
```java
ReferenceQueue<Object> q = new ReferenceQueue<>();
WeakReference<Object> ref = new WeakReference<>(bigObj, q);
if (ref.get() == null) { /* 已清除 */ }
ref.enqueue();                 // 显式入队（测试确定性路径）
Reference<? extends Object> r = q.poll();  // r == ref
Reference.reachabilityFence(x); // 防止提前回收
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.base/share/classes/java/lang/ref/Reference.java`：`reachabilityFence` 为空实现（HotSpot 内联保证语义），构造器对 null queue 使用内部 NULL_QUEUE。
- JDK `test/jdk/java/lang/ref/EarlyTimeout.java`、`EnqueuePollRace.java`、`Basic.java`：以 WeakReference+ReferenceQueue 验证 poll/enqueue 行为；`NullQueue.java` 验证 null queue 上 enqueue 返回 false。
- 开源缓存库（Guava `LocalCache`、Ehcache）用 WeakReference/SoftReference 做条目回收，Guava `FinalizableReference`（`guava/src/com/google/common/base/internal/Finalizer.java`）经子类化 Reference 实现清理。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证 abstract、无 public 构造器、`refersTo` final、`clone` protected、`reachabilityFence` static。
黑盒（全部走强可达/显式 enqueue 确定性路径，不调 System.gc）：
- get/refersTo（同对象 true、异对象 false、null false）→ clear → get null、refersTo(null) true；
- 无队列时 enqueue 返回 false 且不入队；有队列时 enqueue（实现先 clearImpl 清 referent，get 变 null）→isEnqueued→poll 返回同一引用→isEnqueued false→二次 enqueue false；
- clear 后再 enqueue 仍可入队并被 poll 到；
- null 引用对象构造合法；`reachabilityFence` 可任意调用。
对应 `ReferenceTest.java` 的 `testBlackBox()`。
